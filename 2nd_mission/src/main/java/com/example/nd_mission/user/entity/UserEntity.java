package com.example.nd_mission.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    @Setter
    private String username; // 사용자 ID
    @Setter
    private String password;
    @Setter
    private String nickname; // 닉네임
    @Setter
    private String name; // 사용자 이름
    @Setter
    private String age;
    @Setter
    private String email;
    @Setter
    private String phone;
    @Setter
    private String profileImage;
    @Setter
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Setter
    private String businessNum;
}

