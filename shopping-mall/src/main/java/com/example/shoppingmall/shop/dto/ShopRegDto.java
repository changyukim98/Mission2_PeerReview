package com.example.shoppingmall.shop.dto;

import com.example.shoppingmall.shop.entity.ShopCategory;
import lombok.Data;

@Data
public class ShopRegDto {
    private String name;
    private String description;
    private ShopCategory category;
}
