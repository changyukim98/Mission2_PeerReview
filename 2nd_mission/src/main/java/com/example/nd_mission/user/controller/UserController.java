package com.example.nd_mission.user.controller;

import com.example.nd_mission.jwt.JwtTokenUtils;
import com.example.nd_mission.jwt.dto.JwtResponseDto;
import com.example.nd_mission.user.dto.LoginDto;
import com.example.nd_mission.user.dto.RegisterDto;
import com.example.nd_mission.user.dto.UpgradeDto;
import com.example.nd_mission.user.dto.UserDto;
import com.example.nd_mission.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtTokenUtils jwtTokenUtils;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    // username과 password만 입력하면 됨
    @PostMapping("/register")
    public UserDto userRegister(
            @RequestBody
            RegisterDto dto
    ) {
        return userService.createUser(dto);
    }

    // 로그인
    // 이걸 하면 Jwt Token 발급됨
    @PostMapping("/login")
    public JwtResponseDto login(
            @RequestBody
            LoginDto dto
    ) {
        return userService.loginUser(dto);
    }

    // 필수 정보 입력
    // postman에서는 username과 password 동봉해야함!
    @PostMapping("/my_profile/fill_info")
    public ResponseEntity<String> updateInfo(
            @RequestBody
            UserDto dto
    ) {
        UserDto updatedUser = userService.activateUser(dto);
        log.info(updatedUser.getNickname());
        log.info(updatedUser.getName());
        log.info(updatedUser.getAge());
        log.info(updatedUser.getEmail());
        log.info(updatedUser.getPhone());

        // 성공 메시지와 업데이트된 정보 포함하여 반환
        String response = String.format(
                "Profile updated successfully!%n" +
                        "Nickname: %s%n" +
                        "Name: %s%n" +
                        "Age: %s%n" +
                        "Email: %s%n" +
                        "Phone: %s",
                dto.getNickname(), dto.getName(), dto.getAge(), dto.getEmail(), dto.getPhone());
        return new ResponseEntity<>(response, HttpStatus.OK);

        // -> public UserDto updateInfo 로 쓴다면
        // Postman에서 dto의 모든 내용을 반환해서 볼 수 있어요!
        // return userService.activateUser(dto);

    }

    // 사업자 번호 추가로 신청하기
    @PostMapping("/my_profile/{username}/fill_business")
    public ResponseEntity<String> promotion(
            @PathVariable("username")
            String username,
            @RequestBody
            UpgradeDto dto
    ) {
        // 일단 UserEntity에 사업자 번호 추가해주기( = 곧 사업자 번호 신청?)
        userService.applyPromotion(username, dto);
        log.info(userService.applyPromotion(username, dto).getBusinessNum());

        // 사실 이러한 방식은 Postman에서 BusinessNum 잘못 입력해도
        // 항상 신청 완료라고 뜨기는 합니다 ( nullable )
        // 다만 딱히 제한 조건에 대해 만들지 않았기에 일단 HttpStatus.Ok...
        String response = String.format(
                "apply confirmed%n" +
                "BusinessNum : %s", dto.getBusinessNum()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);

        // -> public UserDto applyPromotion
        // return userService.applyPromotion(dto) 로 전체 내용 볼수도 있음
    }

    // 사용자 프로필 이미지 업로드
    // Multipart 공부하고 나중에 구현하도록 하자
    /*
    @PostMapping("/{userId}/profile-image")
    public ResponseEntity<UserDto> uploadProfileImage(
            @PathVariable
            Long userId,
            @RequestParam("image")
            MultipartFile file) {
        UserDto updatedUserDto = userService.updateProfileImage(userId, file);
        return ResponseEntity.ok(updatedUserDto);
    }
    @GetMapping("/my_profile/fill_info_image")
    public UserDto updateProfileImage(
            @RequestBody
            UserDto dto
    ) {
        return userService.uploadImage(dto);
    }
     */


}
