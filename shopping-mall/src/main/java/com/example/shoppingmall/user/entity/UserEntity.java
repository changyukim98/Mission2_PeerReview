package com.example.shoppingmall.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_table")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String username;
    @Setter
    private String password;
    @Setter
    private String nickname;
    @Setter
    private String firstName;
    @Setter
    private String lastName;
    @Setter
    private Integer age;
    @Setter
    private String email;
    @Setter
    private String phone;
    @Setter
    private String profileImagePath;
    @Setter
    @Enumerated(EnumType.STRING)
    private UserRole role;
    @Setter
    private String businessNum;
}