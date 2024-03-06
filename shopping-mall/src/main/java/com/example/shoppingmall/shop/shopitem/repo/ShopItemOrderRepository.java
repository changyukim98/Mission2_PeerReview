package com.example.shoppingmall.shop.shopitem.repo;

import com.example.shoppingmall.shop.shopitem.entity.ShopItemOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopItemOrderRepository
    extends JpaRepository<ShopItemOrder, Long> {
}
