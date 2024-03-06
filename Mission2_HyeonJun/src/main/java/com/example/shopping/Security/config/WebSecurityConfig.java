package com.example.shopping.Security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 모두 허용
                        .requestMatchers(
                                "/no-auth",
                                "/users/home",
                                "/tests"
                        )
                        .permitAll()

                        // 로그인 했으면 접근 불가 페이지
                        .requestMatchers(
                                "/users/login",
                                "/users/register"
                        )
                        .anonymous()

                        // 인증 필요
                        .requestMatchers(
                                "/users/my-profile",
                                "/users/logout"
                        ).authenticated()  // 인증이 필요하다 설정
                )
                // 토큰설정
                .sessionManagement(
                        sessionManagement -> sessionManagement
                                .sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )
                // 로그인 설정
                .formLogin(formLogin -> formLogin
                        .loginPage("/users/login")
                        .defaultSuccessUrl("/users/my-profile")
                        .failureUrl("/users/login?fail")
                )
                .logout(logout -> logout
                        .logoutUrl("/users/logout")
                        .logoutSuccessUrl("/users/login")
                );
        return http.build();
    }

//    @Bean
    public UserDetailsManager userDetailsManager(
            PasswordEncoder passwordEncoder) {
        // 기본용 사용자 "user", "password"
        UserDetails user1 = User.withUsername("user")
                .password(passwordEncoder.encode("password"))
                .build();
        return new InMemoryUserDetailsManager(user1);
    }

    // 비밀번호 암호화해주는 PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
