package com.example.shoppingmall.shop.repo;

import com.example.shoppingmall.shop.entity.Shop;
import com.example.shoppingmall.shop.entity.ShopCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShopRepository
    extends JpaRepository<Shop, Long> {
    Optional<Shop> findByOwnerId(Long id);

    List<Shop> findAllByOrderByLastPurchasedDesc();

    List<Shop> findAllByNameContaining(String nameQ);

    List<Shop> findAllByCategory(ShopCategory category);

    List<Shop> findAllByNameContainingAndCategory(String nameQ, ShopCategory category);


}
