package com.example.shopping.Security.Repository;

import com.example.shopping.Security.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername (String username);
    boolean existsByUsername(String username);
}
