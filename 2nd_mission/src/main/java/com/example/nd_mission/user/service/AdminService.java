package com.example.nd_mission.user.service;


import com.example.nd_mission.user.dto.UserDto;
import com.example.nd_mission.user.entity.UserEntity;
import com.example.nd_mission.user.entity.UserRole;
import com.example.nd_mission.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// 관리자 기능을 담당
public class AdminService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 관리자 로그인


    // 사용자 전환 신청 목록 조회
    public List<UserEntity> getAllApplyUsers() {
        // 사업자 번호 신청한 사용자 목록 조회
        List<UserEntity> applyUsers =
                userRepository.findByRoleAndBusinessNumIsNotNull(UserRole.ROLE_USER);
        if (applyUsers.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return applyUsers;
    }
    // 사업자 사용자 전환 관련

    public void accept() {

    }
    public void decline() {

    }
    // 신청 및 거절

}
