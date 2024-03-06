package com.example.shoppingmall.user.dto;

import com.example.shoppingmall.user.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;

    private String username;
    private String password;
    private String nickname;
    private String firstName;
    private String lastName;
    private Integer age;
    private String email;
    private String phone;
    private String profileImagePath;
    private String role;
    private String businessNum;

    public static UserDto fromEntity(UserEntity entity) {
        return new UserDto(
                entity.getId(),
                entity.getUsername(),
                entity.getPassword(),
                entity.getNickname(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getAge(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getProfileImagePath(),
                entity.getRole().name(),
                entity.getBusinessNum());
    }
}
