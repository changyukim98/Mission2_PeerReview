package com.example.nd_mission.user.service;

/*
import com.example.nd_mission.user.entity.UserEntity;
import com.example.nd_mission.user.entity.UserRole;
import com.example.nd_mission.user.repo.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class JpaUserDetailsManager implements UserDetailsManager {
    private final UserRepository userRepository;

    public JpaUserDetailsManager(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        if (!userExists("admin")) {
            createUser((UserDetails) UserEntity.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("password"))
                    .nickname("운영자")
                    .role(UserRole.ROLE_ADMIN)
                    .build());
        }
    }
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        // 일단 username 찾아보고 공백이면 예외 발생
        Optional<UserEntity> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) throw new UsernameNotFoundException(username);
        UserEntity userEntity = optionalUser.get();
        return (UserDetails) UserEntity.builder()
                .username(userEntity.getUsername())
                .password(userEntity.getPassword())
                .nickname(userEntity.getNickname())
                .name(userEntity.getName())
                .email(userEntity.getEmail())
                .phone(userEntity.getPhone())
                .role(userEntity.getRole())
                .build();

    }
    @Override
    public void createUser(UserDetails user) {

    }

    @Override
    public void updateUser(UserDetails user) {

    }

    @Override
    public void deleteUser(String username) {

    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {

    }

    @Override
    public boolean userExists(String username) {
        return false;
    }


}
 */
