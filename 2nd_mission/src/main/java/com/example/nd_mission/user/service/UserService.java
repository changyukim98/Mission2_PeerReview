package com.example.nd_mission.user.service;



import com.example.nd_mission.jwt.JwtTokenUtils;
import com.example.nd_mission.jwt.dto.JwtResponseDto;
import com.example.nd_mission.user.dto.LoginDto;
import com.example.nd_mission.user.dto.RegisterDto;
import com.example.nd_mission.user.dto.UpgradeDto;
import com.example.nd_mission.user.dto.UserDto;
import com.example.nd_mission.user.entity.UserEntity;
import com.example.nd_mission.user.entity.UserRole;
import com.example.nd_mission.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtils jwtTokenUtils;

    // 유저 생성 (회원가입 로직)
    public UserDto createUser(RegisterDto registerDto) {
        // 만약에 생성하려는 username이 중복이 있다면
        boolean existsByUsername = userRepository.existsByUsername(registerDto.getUsername());

        if (existsByUsername) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        // UserEntity 객체 생성 및 저장
        UserEntity userEntity = UserEntity.builder()
                .username(registerDto.getUsername())
                .password(passwordEncoder.encode(registerDto.getPassword()))
                .role(UserRole.ROLE_INACTIVE) // 기본 권한 설정 (비활성화)
                .build();

        // 저장 후, 저장된 UserEntity를 UserDto로 변환하여 반환
        return UserDto.fromEntity(userRepository.save(userEntity));
    }


    // 유저 로그인 (with Jwt)
    public JwtResponseDto loginUser(LoginDto loginDto) {
        // 사용자 이름으로 사용자 정보 조회
        UserEntity userEntity = userRepository.findByUsername(loginDto.getUsername())
                // 없으면 오류 메세지 출력
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + loginDto.getUsername()));

        // 조회된 사용자 비밀번호와 입력된 비밀번호 일치 여부 확인
        if (passwordEncoder.matches(loginDto.getPassword(), userEntity.getPassword())) {
            // 비밀번호가 일치하면 JWT 토큰 생성
            String token = jwtTokenUtils.generateToken(userEntity);
            // 생성된 토큰을 JwtResponseDto에 담아 반환
            return new JwtResponseDto(token);
        } else {
            // 비밀번호가 일치하지 않으면 예외 발생
            throw new BadCredentialsException("Invaild username or password supplied");
        }
    }

    // 사용자 필수 정보 업데이트(비활성 -> 활성화로)
    public UserDto activateUser(UserDto userDto) {
        // 먼저 username으로 찾아보기
        Optional<UserEntity> optionalUser
                = userRepository.findByUsername(userDto.getUsername());
        // 없으면 오류 발생
        if (optionalUser.isEmpty())
            throw new UsernameNotFoundException(userDto.getUsername());

                /*
        // id 방식으로 찾는게 아니기에 제거
        UserEntity userEntity = userRepository.findById(userDto.getId())
                // 없으면 오류 메세지 출력
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with id: " + userDto.getId()));
                */
        UserEntity userEntity = optionalUser.get();
        // 있다는 가정 하에 필수 정보 업데이트
        userEntity.setNickname(userDto.getNickname());  // 얘는 닉네임
        userEntity.setName(userDto.getName());          // 얘는 이름
        userEntity.setAge(userDto.getAge());            // 연령대
        userEntity.setEmail(userDto.getEmail());        // 이메일
        userEntity.setPhone(userDto.getPhone());        // 전화번호
                // 이미지는 필수 정보에 해당하지 않음
                // userEntity.setProfileImage(userDto.getProfileImage());

        // 지금은 INACTIVE임
        // 관리자가 아니고, 지금 INACTIVE라면
        if (userEntity.getRole() != UserRole.ROLE_ADMIN
                && userEntity.getRole().equals(UserRole.ROLE_INACTIVE) ){
            userEntity.setRole(UserRole.ROLE_USER);     // 비활성 -> 활성
        }
        // ROLE_USER 사용자가 만약 또 정보를 바꾼다고 해도, Set은 잘 작동될것이고
        // ROLE에 대해서는 어자피 바뀔 일이 없으니 그대로임~
        userRepository.save(userEntity);
        return UserDto.fromEntity(userEntity);
    }

    // 사용자 권한 변경(to 사업자로) 신청
    public UserDto applyPromotion(String username, UpgradeDto dto){
        // 1. 먼저 userRepository에서 username으로 사용자를 찾아보고
        Optional<UserEntity> optionalUser = userRepository.findByUsername(username);
        log.info(optionalUser.toString());
        // 2. 없으면 NOT_FOUND 반환
        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        UserEntity userEntity = optionalUser.get();

        userEntity.setBusinessNum(dto.getBusinessNum());    // 사업자 번호 입력
        userRepository.save(userEntity);                    // 저장소 저장
        return UserDto.fromEntity(userEntity);
    }


    // 사용자 프로필 이미지 업로드
    // Multipart는 나중에 구현하도록 하자
    /*
    public UserDto updateProfileImage(Long userId, MultipartFile file) {
        // 1. 파일을 저장하고 파일 경로를 반환받는 로직 구현 필요
        String imagePath = fileStorageService.storeFile(file);

        // 2. 유저 엔티티를 조회
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 3. 이미지 경로를 엔티티에 설정
        userEntity.setProfileImage(imagePath);

        // 4. 변경 사항 저장
        UserEntity updatedUser = userRepository.save(userEntity);

        // 5. DTO 변환 및 반환
        return UserDto.fromEntity(updatedUser);
    }
     */
}