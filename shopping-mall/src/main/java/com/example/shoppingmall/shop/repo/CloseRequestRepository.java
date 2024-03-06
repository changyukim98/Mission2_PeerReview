package com.example.shoppingmall.shop.repo;

import com.example.shoppingmall.shop.entity.ShopCloseRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CloseRequestRepository
    extends JpaRepository<ShopCloseRequest, Long> {
    boolean existsByOwnerId(Long ownerId);
}
