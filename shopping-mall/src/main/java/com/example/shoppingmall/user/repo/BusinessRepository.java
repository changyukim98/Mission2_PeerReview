package com.example.shoppingmall.user.repo;

import com.example.shoppingmall.user.entity.BusinessRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessRepository
    extends JpaRepository<BusinessRegistration, Long> {
    boolean existsByUserId(Long id);
}
