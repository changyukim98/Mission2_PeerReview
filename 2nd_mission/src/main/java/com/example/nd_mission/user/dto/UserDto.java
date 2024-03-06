package com.example.nd_mission.user.dto;

import com.example.nd_mission.user.entity.UserEntity;
import com.example.nd_mission.user.entity.UserRole;
import lombok.*;

@Getter
@Builder
@ToString
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String name;
    private String age;
    private String email;
    private String phone;
    private String profileImage;
    private UserRole role;
    private String businessNum;

    public static UserDto fromEntity(UserEntity entity) {
        return UserDto.builder()
                .username(entity.getUsername())
                .password(entity.getPassword())
                .nickname(entity.getNickname())
                .name(entity.getName())
                .age(entity.getAge())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .profileImage(entity.getProfileImage())
                .role(entity.getRole())
                .businessNum(entity.getBusinessNum())
                .build();

    }
}
