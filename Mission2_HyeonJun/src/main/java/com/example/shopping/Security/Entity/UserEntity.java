package com.example.shopping.Security.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "user table")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Setter
    private String username;    // name

    @Column(nullable = false)
    @Setter
    private String password;    // password
    @Setter
    private String nickname;    // 1. 닉네임
    @Setter
    private String name;        // 2. 이름
    @Setter
    private Integer age;        // 3. 나이
    @Setter
    private String email;       // 4. 이메일
    @Setter
    private String phone;       // 5. 전화번호
    @Setter
    private String role;        // 6. 사업자, 일반 나눌 것
    @Setter
    private String profileImage; // 7. 프로필 이미지 업로드용
    @Setter
    private String businessNumber;

    public static UserEntity fromEntity(CustomUserDetails customUserDetails){
        return UserEntity.builder()
                .id(customUserDetails.getId())
                .username(customUserDetails.getUsername())
                .password(customUserDetails.getPassword())
                .nickname(customUserDetails.getNickname())
                .name(customUserDetails.getName())
                .age(customUserDetails.getAge())
                .email(customUserDetails.getEmail())
                .phone(customUserDetails.getPhone())
                .profileImage(customUserDetails.getProfileImage())
                .role(customUserDetails.getRole())
                .businessNumber(customUserDetails.getBusinessNumber())
                .build();
    }
}
