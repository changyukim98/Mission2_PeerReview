package com.example.shoppingmall.shop.shopitem.entity;

import com.example.shoppingmall.shop.entity.Shop;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String name;
    @Setter
    private String description;
    @Setter
    private Long price;
    @Setter
    private String imagePath;
    @Setter
    private Integer stock;

    @ManyToOne
    private Shop shop;
}
