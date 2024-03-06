package com.example.nd_mission.user.controller;

import com.example.nd_mission.user.entity.UserEntity;
import com.example.nd_mission.user.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    // 관리자 전용 로그인 (보안+)
    @PostMapping("/login")
    public String adminLogin() {
        return "Not done yet";
    }

    // 관리자가 신청 목록 확인
    @GetMapping("/confirm_list")
    public List<UserEntity> confirmLists() {
        return adminService.getAllApplyUsers();
    }

    // 관리자가 신청 승인 or 신청 거부
    @PostMapping("/confirm_list/{username}/accept")
    public String acceptApply(
            @PathVariable("username")
            String username
    ) {
        adminService.accept();
        return "OK";
    }
    @PostMapping("/confirm_list/{username}/decline")
    public String declineApply(
            @PathVariable("username")
            String username
    ) {
        adminService.decline();
        return "NO";
    }
}
