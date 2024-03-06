package com.example.shoppingmall.user.repo;

import com.example.shoppingmall.user.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAttemptRepository
    extends JpaRepository<LoginAttempt, Long> {
}
