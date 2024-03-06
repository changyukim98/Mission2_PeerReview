package com.example.nd_mission.usedProducts.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 중고 물품에 대한 Entity
public class UsedProductEntity {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    @Setter
    @NotNull
    private String title;
    @Setter
    @NotNull
    private String description;
    @Setter
    private String image;
    @Setter
    @NotNull
    private Integer minCost;
    @Setter
    @Enumerated
    private UsedProductStateEntity statement;
}