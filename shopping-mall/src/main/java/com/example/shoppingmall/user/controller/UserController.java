package com.example.shoppingmall.user.controller;

import com.example.shoppingmall.user.dto.*;
import com.example.shoppingmall.user.entity.BusinessRegistration;
import com.example.shoppingmall.jwt.JwtResponseDto;
import com.example.shoppingmall.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/login")
    public JwtResponseDto userLogin(
            @RequestBody
            LoginDto dto
    ) {
        return userService.loginUser(dto);
    }

//    @PostMapping("/login/captcha")
//    public String loginCaptcha(
//            @RequestBody
//
//    ) {
//
//    }


    @PostMapping("/register")
    public UserDto userRegister(
            @RequestBody
            RegisterDto dto
    ) {
        return userService.createUser(dto);
    }

    @PostMapping("/fill-essential")
    public UserDto fillEssential(
            @RequestBody
            EssentialInfoDto dto
    ) {
        return userService.fillEssential(dto);
    }

    @PostMapping("/update-avatar")
    public UserDto updateAvatar(
            @RequestParam("image")
            MultipartFile image
    ) {
        String profilePath = userService.saveProfileImage(image);
        return userService.updateProfileImage(profilePath);
    }

    @PostMapping("/business")
    public BusinessRegDto businessRegister(
            @RequestParam("business-number")
            String businessNumber
    ) {
        return userService.businessRegister(businessNumber);
    }

    @GetMapping("/business")
    public List<BusinessRegistration> readBusinessRegistrations() {
        return userService.readBusinessRegistration();
    }

    @PostMapping("/business/{id}/accept")
    public String acceptBusinessRegistration(
            @PathVariable("id")
            Long id
    ) {
        userService.acceptBusinessRegistration(id);
        return "done";
    }

    @PostMapping("/business/{id}/decline")
    public String declineBusinessRegistration(
            @PathVariable("id")
            Long id
    ) {
        userService.declineBusinessRegistration(id);
        return "done";
    }
}
