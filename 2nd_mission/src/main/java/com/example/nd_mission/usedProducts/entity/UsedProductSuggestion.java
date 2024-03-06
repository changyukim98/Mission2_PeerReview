package com.example.nd_mission.usedProducts.entity;

import com.example.nd_mission.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
// 한 상품에 대해 누가, 그리고 어떤 가격으로, 어떤 제안을 했는지 나타내는 Entity
public class UsedProductSuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private UserEntity userEntity;

    @ManyToOne
    private UsedProductEntity usedProductEntity;

    private Integer offeredPrice;

    @Enumerated
    private UsedProductStateEntity state;
}
