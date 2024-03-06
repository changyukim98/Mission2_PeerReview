package com.example.shoppingmall.useditem.dto;

import com.example.shoppingmall.useditem.entity.UsedItemStatus;
import com.example.shoppingmall.useditem.entity.UsedItem;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsedItemDto {
    private Long id;

    @Setter
    private String title;
    @Setter
    private String description;
    @Setter
    private Integer price;
    @Setter
    private String imagePath;
    @Setter
    private UsedItemStatus status;

    private Long userId;

    public static UsedItemDto fromEntity(UsedItem entity) {
        return UsedItemDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .imagePath(entity.getImagePath())
                .status(entity.getStatus())
                .userId(entity.getUser().getId())
                .build();
    }
}
