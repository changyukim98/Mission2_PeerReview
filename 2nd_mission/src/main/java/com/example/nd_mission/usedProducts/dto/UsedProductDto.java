package com.example.nd_mission.usedProducts.dto;

import com.example.nd_mission.usedProducts.entity.UsedProductEntity;
import com.example.nd_mission.usedProducts.entity.UsedProductStateEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@AllArgsConstructor
public class UsedProductDto {
    private Long id;
    private String title;
    private String description;
    private String image;
    private Integer minCost;
    private UsedProductStateEntity statement;

    public static UsedProductDto fromEntity(UsedProductEntity entity) {
        return UsedProductDto.builder()
                .title(entity.getTitle())
                .description(entity.getDescription())
                .image(entity.getImage())
                .minCost(entity.getMinCost())
                .statement(entity.getStatement())
                .build();
    }
}
