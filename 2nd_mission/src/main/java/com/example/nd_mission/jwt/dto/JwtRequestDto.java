package com.example.nd_mission.jwt.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtRequestDto {
    private String username;
    private String password;
}
