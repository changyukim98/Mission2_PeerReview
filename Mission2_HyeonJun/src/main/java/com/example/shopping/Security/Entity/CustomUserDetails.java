package com.example.shopping.Security.Entity;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

// UserEntity 를 바탕으로 Spring Security 내부에서
// 사용자 정보를 주고, 받기 위한 객체임을 나타내는 interface UserDetails
// 의 커스텀 구현체
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    @Getter
    private Long id;
    private String username;
    private String password;

    @Getter
    @Setter
    private String nickname;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Integer age;

    @Getter
    @Setter
    private String email;

    @Getter
    @Setter
    private String phone;

    @Getter
    @Setter
    private String profileImage;

    @Getter
    @Setter
    private String role;            // 역할 나눌 것

    @Getter
    @Setter
    private String businessNumber;  // 사업자 번호

    // 권한 데이터를 담기 위한 속성
    private String authorities;

    public String getRawAuthorities() {
        return this.authorities;
    }

    @Override
    // ROLE_USER,ROLE_ADMIN,READ_AUTHORITY,WRITE_AUTHORITY
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> grantedAuthorities
                = new ArrayList<>();
        String[] rawAuthorities = authorities.split(",");
        for (String rawAuthority : rawAuthorities) {
            grantedAuthorities.add(new SimpleGrantedAuthority(rawAuthority));
        }

        return grantedAuthorities;
        // return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }


    // 먼 미래
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}