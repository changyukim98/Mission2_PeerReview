package com.example.shoppingmall.shop.shopitem.repo;

import com.example.shoppingmall.shop.shopitem.entity.ShopItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShopItemRepository
        extends JpaRepository<ShopItem, Long> {

    @Query("SELECT s FROM ShopItem s " +
            "WHERE s.name LIKE CONCAT('%', :nameQ, '%')" +
            "AND s.price >= :priceMin " +
            "AND s.price <= :priceMax")
    List<ShopItem> searchShopItems(
            @Param("nameQ") String nameQ,
            @Param("priceMin") Long priceMin,
            @Param("priceMax") Long priceMax
    );
}
