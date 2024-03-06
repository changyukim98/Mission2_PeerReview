package com.example.shopping.dto;

import lombok.Data;

@Data
public class RegisterDto {
    // 로그인은 아이디 비밀번호만
    private String username;
    private String password;
}
