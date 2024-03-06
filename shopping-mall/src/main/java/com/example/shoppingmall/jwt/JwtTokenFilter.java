package com.example.shoppingmall.jwt;

import com.example.shoppingmall.user.entity.UserEntity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
public class JwtTokenFilter extends OncePerRequestFilter {
    private final JwtTokenUtils jwtTokenUtils;

    public JwtTokenFilter(JwtTokenUtils jwtTokenUtils) {
        this.jwtTokenUtils = jwtTokenUtils;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.split(" ")[1];
            if (jwtTokenUtils.validate(token)) {
                String username = jwtTokenUtils
                        .parseClaims(token)
                        .getSubject();

                SecurityContext context
                        = SecurityContextHolder.createEmptyContext();
                AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        UserEntity.builder()
                                .username(username)
                                .build(),
                        token, new ArrayList<>());
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } else {
                log.warn("jwt validation failed");
            }
        }
        filterChain.doFilter(request, response);
    }
}
