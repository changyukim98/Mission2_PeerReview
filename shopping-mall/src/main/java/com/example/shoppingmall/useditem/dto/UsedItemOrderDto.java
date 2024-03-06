package com.example.shoppingmall.useditem.dto;

import com.example.shoppingmall.useditem.entity.UsedItemOrderStatus;
import com.example.shoppingmall.useditem.entity.UsedItemOrder;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsedItemOrderDto {
    private Long id;

    @Setter
    private Long itemId;
    @Setter
    private Long buyerId;
    @Setter
    private UsedItemOrderStatus status;

    public static UsedItemOrderDto fromEntity(UsedItemOrder entity) {
        return UsedItemOrderDto.builder()
                .id(entity.getId())
                .itemId(entity.getItem().getId())
                .buyerId(entity.getBuyer().getId())
                .status(entity.getStatus())
                .build();
    }
}
