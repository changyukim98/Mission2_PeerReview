package com.example.shopping.Security.Service;

import com.example.shopping.Jwt.JwtResponseDto;
import com.example.shopping.Jwt.JwtTokenUtils;
import com.example.shopping.Security.Entity.BusinessEntity;
import com.example.shopping.Security.Entity.CustomUserDetails;
import com.example.shopping.Security.Entity.UserEntity;
import com.example.shopping.Security.Repository.BusinessRepository;
import com.example.shopping.Security.Repository.UserRepository;
import com.example.shopping.dto.CurrentDto;
import com.example.shopping.dto.EssentialDto;
import com.example.shopping.dto.RegisterDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JpaUserDetailsManager jpaUserDetailsManager;
    private final BusinessRepository businessRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtils jwtTokenUtils;

    // 계정 생성
    public void createUser(
            RegisterDto registerDto
    ){
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .username(registerDto.getUsername())
                .password(passwordEncoder.encode(registerDto.getPassword()))
                .role("ROLE_INACTIVE")
                .build();
        jpaUserDetailsManager.createUser(userDetails);
    }

    // 토큰생성
    public JwtResponseDto currentUser(CurrentDto currentDto) {
        if (!jpaUserDetailsManager.userExists(currentDto.getUsername()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        UserDetails userDetails
                = jpaUserDetailsManager.loadUserByUsername(currentDto.getUsername());

        if (!passwordEncoder.matches(currentDto.getPassword(), userDetails.getPassword()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        String token = jwtTokenUtils.generateToken(userDetails);
        return new JwtResponseDto(token);
    }

    public void essentialInfo(EssentialDto dto){
        CustomUserDetails userDetails = getCurrentUserDetails();

        userDetails.setNickname(dto.getNickname());
        userDetails.setName(dto.getName());
        userDetails.setAge(dto.getAge());
        userDetails.setEmail(dto.getEmail());
        userDetails.setPhone(dto.getPhone());
        if (userDetails.getRole().equals("ROLE_INACTIVE"))
            userDetails.setRole("ROLE_USER");
        jpaUserDetailsManager.updateUser(userDetails);
    }

    public CustomUserDetails getCurrentUserDetails() {
        String username
                = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            return (CustomUserDetails) jpaUserDetailsManager.loadUserByUsername(username);
        } catch (ClassCastException e) {
            log.error("Failed Cast to: {}", CustomUserDetails.class);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void updateProfileImage(String profileImage){
        CustomUserDetails userDetails = getCurrentUserDetails();

        userDetails.setProfileImage(profileImage);
        jpaUserDetailsManager.updateUser(userDetails);
    }

    public String saveImage(MultipartFile saveImage){
        String profileDirection = "media/profile/";

        // 유저 이름대로 파일 만들기
        // 경로부터
        try{
            Files.createDirectories(Path.of("media/profile/"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 유저 이름
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 파일 이름
        String Filename = saveImage.getOriginalFilename();
        String[] fileNameSplit = Filename.split("\\.");
        String extension = fileNameSplit[fileNameSplit.length -1];
        String profileFilename = username + "."  + extension;

        // 파일 경로
        String profilePath = profileDirection + profileFilename;

        // 저장
        try {
            saveImage.transferTo(Path.of(profilePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return profilePath;
    }

    @Transactional
    public  void businessRegister(String businessNumber){
        CustomUserDetails userDetails = getCurrentUserDetails();

        // 사업자 유저로
        if (!userDetails.getRole().equals("ROLE_USER"))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        BusinessEntity businessEntity = BusinessEntity.builder()
                .user((UserEntity) jpaUserDetailsManager.loadUserByUsername(userDetails.getUsername()))
                .business(businessNumber)
                .build();
    }

    public void acceptBusinessRole(Long id){
        CustomUserDetails userDetails = getCurrentUserDetails();

        // ROLE_ADMIN 에서만 승인가능
        if (!userDetails.getRole().equals("ROLE_ADMIN"))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        // 가입 신청이 존재하지 않는 경우
        Optional<BusinessEntity> optionalRegistration = businessRepository.findById(id);
        if (optionalRegistration.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        BusinessEntity registration = optionalRegistration.get();
        UserEntity userEntity = registration.getUser();

        // 일반 유저일 경우에만 업그레이드 가능
        if (!userEntity.getRole().equals("ROLE_USER"))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        userEntity.setRole("ROLE_BUSINESS");
        userRepository.save(userEntity);
        businessRepository.delete(registration);
    }
}
