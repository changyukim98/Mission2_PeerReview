package com.example.shoppingmall.shop.dto;

import com.example.shoppingmall.shop.entity.ShopCloseRequest;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopCloseResponseDto {
    private Long id;
    private String reason;
    private Long ownerId;

    public static ShopCloseResponseDto fromEntity(ShopCloseRequest entity) {
        return ShopCloseResponseDto.builder()
                .id(entity.getId())
                .reason(entity.getReason())
                .ownerId(entity.getOwner().getId())
                .build();
    }
}
