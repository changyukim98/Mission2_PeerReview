package com.example.shoppingmall.shop.shopitem.dto;

import com.example.shoppingmall.shop.shopitem.entity.ShopItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopItemRequest {
    private String name;
    private String description;
    private Long price;
    private Integer stock;
}
