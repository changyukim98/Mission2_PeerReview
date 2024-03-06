package com.example.shoppingmall.user.service;

import com.example.shoppingmall.AuthenticationFacade;
import com.example.shoppingmall.api.captcha.NcpCaptchaApiService;
import com.example.shoppingmall.shop.repo.ShopRepository;
import com.example.shoppingmall.shop.entity.Shop;
import com.example.shoppingmall.shop.entity.ShopStatus;
import com.example.shoppingmall.user.dto.*;
import com.example.shoppingmall.user.entity.LoginAttempt;
import com.example.shoppingmall.user.entity.UserRole;
import com.example.shoppingmall.user.entity.BusinessRegistration;
import com.example.shoppingmall.user.entity.UserEntity;
import com.example.shoppingmall.jwt.JwtTokenUtils;
import com.example.shoppingmall.jwt.JwtResponseDto;
import com.example.shoppingmall.user.repo.BusinessRepository;
import com.example.shoppingmall.user.repo.LoginAttemptRepository;
import com.example.shoppingmall.user.repo.UserRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final NcpCaptchaApiService ncpApiService;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final ShopRepository shopRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationFacade facade;
    private final Gson gson;

    // 유저 계정 생성
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

    public UserDto updateProfileImage(String profileImagePath) {
        UserEntity userEntity = facade.getCurrentUserEntity();

        userEntity.setProfileImagePath(profileImagePath);
        return UserDto.fromEntity(userRepository.save(userEntity));
    }

    public String saveProfileImage(MultipartFile image) {
        UserEntity currentUser = facade.getCurrentUserEntity();

        String profileDir = "media/profile/";

        // 폴더 만들기
        try {
            Files.createDirectories(Path.of(profileDir));
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 유저 이름 가져오기
        String username = currentUser.getUsername();

        // 파일 이름
        String originalFilename = image.getOriginalFilename();
        String[] fileNameSplit = originalFilename.split("\\.");
        String extension = fileNameSplit[fileNameSplit.length - 1];
        String profileFilename = username + "." + extension;

        // 파일 경로
        String profilePath = profileDir + profileFilename;
        // 저장
        try {
            image.transferTo(Path.of(profilePath));
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return profilePath;
    }

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
}