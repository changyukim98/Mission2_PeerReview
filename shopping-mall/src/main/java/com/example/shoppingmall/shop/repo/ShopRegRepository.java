package com.example.shoppingmall.shop.repo;

import com.example.shoppingmall.shop.entity.ShopRegStatus;
import com.example.shoppingmall.shop.entity.ShopRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ShopRegRepository
    extends JpaRepository<ShopRegistration, Long> {

    List<ShopRegistration> findAllByOwnerId(Long id);


    boolean existsByOwnerIdAndStatus(Long ownerId, ShopRegStatus status);
}
