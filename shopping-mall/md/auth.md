# 사용자 인증 및 권한 처리

## 사용자 회원 가입

```Java
package com.example.shoppingmall.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_table")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String username;
    @Setter
    private String password;
    @Setter
    private String nickname;
    @Setter
    private String firstName;
    @Setter
    private String lastName;
    @Setter
    private Integer age;
    @Setter
    private String email;
    @Setter
    private String phone;
    @Setter
    private String profileImagePath;
    @Setter
    @Enumerated(EnumType.STRING)
    private UserRole role;
    @Setter
    private String businessNum;
}
```

```Java
    public UserDto createUser(RegisterDto registerDto) {
        // 아이디 중복
        if (userRepository.existsByUsername(registerDto.getUsername()))
            throw new ResponseStatusException(HttpStatus.CONFLICT);

        UserEntity userEntity = UserEntity.builder()
                .username(registerDto.getUsername())
                .password(passwordEncoder.encode(registerDto.getPassword()))
                .role(UserRole.ROLE_INACTIVE)
                .build();
        return UserDto.fromEntity(userRepository.save(userEntity));
    }
```
> UserDto를 통해 아이디, 패스워드를 제시한다.
아이디가 중복되지 않는다면 가입을 진행하고, 해당 UserEntity의 Dto를 반환한다.


## 사용자 로그인

```Java
    // 로그인 정보를 바탕으로 토큰 생성
    public JwtResponseDto loginUser(LoginDto loginDto) {
        if (!userRepository.existsByUsername(loginDto.getUsername()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Optional<UserEntity> optionalUser =
                userRepository.findByUsername(loginDto.getUsername());
        if (optionalUser.isEmpty())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        UserEntity userEntity = optionalUser.get();

        if (!passwordEncoder.matches(loginDto.getPassword(), userEntity.getPassword()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        String keyJsonString = ncpApiService.getCaptchaKey(0);

        JsonObject keyJsonObject = gson.fromJson(keyJsonString, JsonObject.class);
        String captchaKey = keyJsonObject.get("key").getAsString();

        byte[] imageBytes = ncpApiService.getCaptchaImage(captchaKey);

        String fileName = UUID.randomUUID().toString() + "_"
                + userEntity.getUsername() + ".png";
        String filePath = "media/captcha/" + fileName;

        try {
            InputStream inputStream = new ByteArrayInputStream(imageBytes);
            BufferedImage bufferedImage = ImageIO.read(inputStream);

            File outputFile = new File(filePath);
            ImageIO.write(bufferedImage, "png", outputFile);
            System.out.println("이미지가 성공적으로 저장되었습니다.");
        } catch (IOException e) {
            System.err.println("이미지 저장 중 오류가 발생했습니다: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        LoginAttempt loginAttempt = LoginAttempt.builder()
                .username(userEntity.getUsername())
                .captchaKey(captchaKey)
                .imagePath(filePath)
                .build();
        loginAttemptRepository.save(loginAttempt);

        String token = jwtTokenUtils.generateToken(userEntity);
        return new JwtResponseDto(token);
    }
```

```Java
    public String generateToken(UserEntity userEntity) {
        Instant now = Instant.now();
        Claims jwtClaims = Jwts.claims()
                .setSubject(userEntity.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(60 * 60)));

        return Jwts.builder()
                .setClaims(jwtClaims)
                .signWith(this.siginingKey)
                .compact();
    }
```
> LoginDto로 username과 password를 입력받아 로그인에 문제가 없다면 진행하고, 사용자의 username을 담은 Jwt 토큰을 발행한다.  
Captcha를 사용해 사용자의 로그인을 검증하는 부분은 미완성이다.


## Jwt 필터
```Java
package com.example.shoppingmall.jwt;

import com.example.shoppingmall.user.entity.UserEntity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {
    private final JwtTokenUtils jwtTokenUtils;

    public JwtTokenFilter(JwtTokenUtils jwtTokenUtils) {
        this.jwtTokenUtils = jwtTokenUtils;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.split(" ")[1];
            if (jwtTokenUtils.validate(token)) {
                String username = jwtTokenUtils
                        .parseClaims(token)
                        .getSubject();

                SecurityContext context
                        = SecurityContextHolder.createEmptyContext();
                AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        UserEntity.builder()
                                .username(username)
                                .build(),
                        token, new ArrayList<>());
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } else {
                log.warn("jwt validation failed");
            }
        }
        filterChain.doFilter(request, response);
    }
}
```
> 매 서버 접속시마다 받는 Bearer Token으로부터 Jwt 토큰을 분리하고, 해당 Jwt 토큰이 유효한지 검사한 후, Jwt 토큰으로부터 username을 얻어내 SecurityContext에 저장한다.

## 필수 정보 입력
```Java
    // 필수 정보 입력
    public UserDto fillEssential(EssentialInfoDto dto) {
        UserEntity userEntity = facade.getCurrentUserEntity();

        userEntity.setNickname(dto.getNickname());
        userEntity.setFirstName(dto.getFirstName());
        userEntity.setLastName(dto.getLastName());
        userEntity.setAge(dto.getAge());
        userEntity.setEmail(dto.getEmail());
        userEntity.setPhone(dto.getPhone());
        if (userEntity.getRole().equals(UserRole.ROLE_INACTIVE))
            userEntity.setRole(UserRole.ROLE_USER);
        return UserDto.fromEntity(userRepository.save(userEntity));
    }
```
| 사용자의 필수 정보를 입력받고 사용자의 UserRole을 일반 사용자로 바꾼다.

## Admin 사용자 설정
```Java
package com.example.shoppingmall.config;

import com.example.shoppingmall.user.entity.UserEntity;
import com.example.shoppingmall.user.entity.UserRole;
import com.example.shoppingmall.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class UserConfig {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner createAdminUser() {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                UserEntity admin = UserEntity.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("1234"))
                        .role(UserRole.ROLE_ADMIN)
                        .build();
                userRepository.save(admin);
            }
        };
    }
}
```
> CommandLineRunner를 사용해 스프링 프로젝트가 실행될 때 admin 유저를 생성하는 메서드가 실행될 수 있도록 한다.

## 사업자 회원 전환 신청

```Java
package com.example.shoppingmall.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @OneToOne
    private UserEntity user;
    @Setter
    private String businessNum;
}
```

```Java
    @Transactional
    public BusinessRegDto businessRegister(String businessNum) {
        UserEntity userEntity = facade.getCurrentUserEntity();

        // ROLE_USER에서만 사업자 유저로 업그레이드 신청 가능
        if (!userEntity.getRole().equals(UserRole.ROLE_USER))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        if (businessRepository.existsByUserId(userEntity.getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        BusinessRegistration businessRegistration
                = BusinessRegistration.builder()
                .user(userEntity)
                .businessNum(businessNum)
                .build();
        return BusinessRegDto.fromEntity(businessRepository.save(businessRegistration));
    }
```
> businessNum을 사용자로부터 입력받아 BusinessRegistraion이라는 Entity를 만들어 신청을 등록한다.

## Admin의 사업자 회원 전환 신청 조회/허가/거절
```Java
    public List<BusinessRegistration> readBusinessRegistration() {
        UserEntity userEntity = facade.getCurrentUserEntity();

        // ROLE_ADMIN에서만 조회 가능
        if (!userEntity.getRole().equals(UserRole.ROLE_ADMIN))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        return businessRepository.findAll();
    }

    @Transactional
    public void acceptBusinessRegistration(Long id) {
        UserEntity currentUser = facade.getCurrentUserEntity();

        // ROLE_ADMIN에서만 승인 가능
        if (!currentUser.getRole().equals(UserRole.ROLE_ADMIN))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        // 가입 신청이 존재하지 않는 경우
        Optional<BusinessRegistration> optionalRegistration = businessRepository.findById(id);
        if (optionalRegistration.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        BusinessRegistration registration = optionalRegistration.get();
        UserEntity register = registration.getUser();

        // 일반 유저일 경우에만 업그레이드 가능
        if (!register.getRole().equals(UserRole.ROLE_USER))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        register.setRole(UserRole.ROLE_BUSINESS);
        userRepository.save(register);
        businessRepository.delete(registration);

        // user에게 준비중인 Shop 추가
        Shop shop = Shop.builder()
                .status(ShopStatus.PREPARING)
                .owner(register)
                .build();
        shopRepository.save(shop);
    }

    public void declineBusinessRegistration(Long id) {
        UserEntity currentUser = facade.getCurrentUserEntity();

        // ROLE_ADMIN에서만 거절 가능
        if (!currentUser.getRole().equals(UserRole.ROLE_ADMIN))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        // 가입 신청이 존재하지 않는 경우
        if (!businessRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        businessRepository.deleteById(id);
    }
```
> ROLE_ADMIN인 유저는 사업자 전환 신청 현황을 조회할 수 있고, 조회결과로 얻은 신청 id를 바탕으로 허가 및 거절을 할 수 있다.