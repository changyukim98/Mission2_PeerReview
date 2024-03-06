package com.example.shopping.Security.Service;

import com.example.shopping.Security.Entity.CustomUserDetails;
import com.example.shopping.Security.Repository.UserRepository;
import com.example.shopping.Security.Entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Slf4j
//@RequiredArgsConstructor
@Service
public class JpaUserDetailsManager implements UserDetailsManager {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public JpaUserDetailsManager(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        createUser(CustomUserDetails.builder()
                .username("user")
                .password(passwordEncoder.encode("password"))
                .role("role_user")
                .build());
    }


    @Override
    public void createUser(UserDetails user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try{
            // CustomUserDetails 로 수정
            CustomUserDetails userDetails = (CustomUserDetails) user;
            UserEntity newUser = UserEntity.fromEntity(userDetails);
            userRepository.save(newUser);
            /*
//            UserEntity newUser = UserEntity.builder()
//                    .username(userDetails.getUsername())
//                    .password(passwordEncoder.encode(userDetails.getPassword()))
//                    .nickname(userDetails.getNickname())
//                    .name(userDetails.getName())
//                    .age(userDetails.getAge())
//                    .email(userDetails.getEmail())
//                    .phone(userDetails.getPhone())
//                    .build();
//            userRepository.save(newUser);
*/
        }catch (ClassCastException e){
            log.error("Failed Cast to: {}", CustomUserDetails.class);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void updateUser(UserDetails user) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) user;
            UserEntity userEntity = UserEntity.fromEntity(userDetails);
            userRepository.save(userEntity);
        }catch (ClassCastException e){
            log.error("Failed Cast to: {}", CustomUserDetails.class);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteUser(String username) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    // Spring Security 내부에서
    // 인증을 처리할 때 사용하는 메서드임. 반드시 구현되어있어야함.
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) throw new UsernameNotFoundException(username);

        // CustomUserDetails 객체 생성하여 반환
        UserEntity userEntity = optionalUser.get();
        return CustomUserDetails.builder()
                .username(userEntity.getUsername())
                .password(userEntity.getPassword())
                .nickname(userEntity.getNickname())
                .name(userEntity.getName())
                .age(userEntity.getAge())
                .phone(userEntity.getPhone())
                .build();
    }
}