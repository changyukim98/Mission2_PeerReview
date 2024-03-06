package com.example.shoppingmall.shop.shopitem.dto;

import com.example.shoppingmall.shop.shopitem.entity.ShopItemOrder;
import com.example.shoppingmall.shop.shopitem.entity.ShopItemOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopItemOrderResponse {
    private Long id;
    private Long shopItemId;
    private Long customerId;
    private Integer quantity;
    private ShopItemOrderStatus status;

    public static ShopItemOrderResponse fromEntity(ShopItemOrder entity) {
        return ShopItemOrderResponse.builder()
                .id(entity.getId())
                .shopItemId(entity.getShopItem().getId())
                .customerId(entity.getCustomer().getId())
                .quantity(entity.getQuantity())
                .status(entity.getStatus())
                .build();
    }
}
