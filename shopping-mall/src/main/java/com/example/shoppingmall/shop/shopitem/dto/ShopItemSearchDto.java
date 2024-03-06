package com.example.shoppingmall.shop.shopitem.dto;

import com.example.shoppingmall.shop.dto.ShopDto;
import com.example.shoppingmall.shop.shopitem.entity.ShopItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopItemSearchDto {
    private Long id;
    private String name;
    private String description;
    private Long price;
    private String imagePath;
    private String category;
    private String subCategory;
    private Integer stock;
    private ShopDto shopDto;

    public static ShopItemSearchDto fromEntity(ShopItem entity) {
        return ShopItemSearchDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .imagePath(entity.getImagePath())
                .stock(entity.getStock())
                .shopDto(ShopDto.fromEntity(entity.getShop()))
                .build();
    }
}
