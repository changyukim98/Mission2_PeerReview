package com.example.shoppingmall.shop.shopitem.entity;

import com.example.shoppingmall.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopItemOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private ShopItem shopItem;
    @ManyToOne
    private UserEntity customer;

    private Integer quantity;
    @Setter
    private ShopItemOrderStatus status;
}
