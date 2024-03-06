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
public class ShopItemResponse {
    private Long id;
    private String name;
    private String description;
    private Long price;
    private String imagePath;
    private String category;
    private String subCategory;
    private Integer stock;

    public static ShopItemResponse fromEntity(ShopItem entity) {
        return ShopItemResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .imagePath(entity.getImagePath())
                .stock(entity.getStock())
                .build();
    }
}
