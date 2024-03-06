package com.example.nd_mission.config;

import com.example.nd_mission.jwt.JwtTokenFilter;
import com.example.nd_mission.jwt.JwtTokenUtils;
import com.example.nd_mission.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenUtils jwtTokenUtils;
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                auth -> auth
                        // 1. 어떤 경우도 접근 가능함
                        .requestMatchers(
                                "/home/test")
                        .permitAll()

                        // 2. 인증이 안된 경우만 접근 가능함
                        .requestMatchers(
                                "/users/login",
                                "/users/register",
                                "/admin/login")
                        .anonymous()

                        // 3. 인증이 된 경우만 접근 가능함
                        .requestMatchers(
                                "/users/my_profile/fill_info",
                                "/users/my_profile/{username}/fill_business",
                                "/admin/confirm_list",
                                "/admin/confirm_list/{username}/accept",
                                "/admin/confirm_list/{username}/decline"
                        )
                        .authenticated()
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 특정 필터 앞에 나만의 필터를 넣는 곳
                .addFilterBefore(
                        new JwtTokenFilter(jwtTokenUtils),
                        AuthorizationFilter.class
                )
        ;
        return http.build();
    }
    /*
    @Bean
    public UserDetailsManager userDetailsManager(
            PasswordEncoder passwordEncoder
    ) {
        // 테스트용 사용자 1
        UserDetails user1 = User.withUsername("user1")
                .password(passwordEncoder.encode("password1"))
                .build();
        // Spring Security에서 기본으로 제공하는,
        // 메모리 기반 사용자 관리 클래스 + 사용자 1
        return new InMemoryUserDetailsManager(user1);
    }
     */
}
