package com.example.nd_mission.jwt;

import com.example.nd_mission.user.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenUtils {
    // jwt 번역기
    private final JwtParser jwtParser;
    // jwt 암호키
    private final Key signingKey;
    public JwtTokenUtils (
            @Value("${jwt.secret}")
            String jwtSecret
    ) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.jwtParser = Jwts
                .parserBuilder()
                .setSigningKey(this.signingKey)
                .build();
    }

    // UserEntity의 정보를 받아서 토큰 생성
    public String generateToken(UserEntity userEntity) {
        // sub : 담고자 하는 사람
        // iat : 언제 발급되었는가
        // exp : 언제 만료되는가

        Instant now = Instant.now();
        Claims jwtClaims = Jwts.claims()
                .setSubject(userEntity.getUsername())
                // 현재로부터
                .setIssuedAt(Date.from(now))
                // 테스트용 (60초)
                // .setExpiration(Date.from(now.plusSeconds(60L)))
                // 실제 설정값 (1시간)
                .setExpiration(Date.from(now.plusSeconds(60 * 60 )));

        return Jwts.builder()
                .setClaims(jwtClaims)
                .signWith(this.signingKey)
                .compact();
    }

    // 정상적인 JWT인지를 판단하는 메서드
    public boolean validate(String token) {
        // 정상적이지 않은 JWT 라면 예외 발생
        try {
            jwtParser.parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.warn("invaild jwt");
        }
        return false;
    }

    // 실제 데이터를 반환하는 메서드
    public Claims parseClaims(String token) {
        return jwtParser
                .parseClaimsJws(token)
                .getBody();
    }
}
