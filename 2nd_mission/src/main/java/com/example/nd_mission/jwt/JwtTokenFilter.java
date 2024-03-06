package com.example.nd_mission.jwt;

import com.example.nd_mission.user.entity.UserEntity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
    private final JwtTokenUtils jwtTokenUtils;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // 1. header에 Authorization 가져오기
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 2. 헤더가 존재하는지, 그리고 Bearer로 시작하는지
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.split(" ")[1];
            // 3. Token이 유효한 토큰인지
            if (jwtTokenUtils.validate(token)) {
                // 4. 유효하다면 사용자 정보를 SecurityContext에 등록
                SecurityContext context = SecurityContextHolder.createEmptyContext();

                // 사용자 정보 회수
                String username = jwtTokenUtils
                        .parseClaims(token)
                        .getSubject();
                // 인증 정보 생성
                AbstractAuthenticationToken authentication
                        = new UsernamePasswordAuthenticationToken(
                                UserEntity.builder()
                                        .username(username)
                                        .build(),
                        token, new ArrayList<>()
                );
                // 인증 정보 등록
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
                log.info("set security context with jwt");
            } else {
                log.warn("jwt validation failed");
            }
        }
        // 5. 다음 필터 호출
        filterChain.doFilter(request, response);
    }
}
