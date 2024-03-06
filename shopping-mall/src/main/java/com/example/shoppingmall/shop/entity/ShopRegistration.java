package com.example.shoppingmall.shop.entity;

import com.example.shoppingmall.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private String name;
    @Setter
    private String description;
    @Setter
    @Enumerated(EnumType.STRING)
    private ShopCategory category;
    @Setter
    @Enumerated(EnumType.STRING)
    private ShopRegStatus status;
    @Setter
    private String declineReason;

    @ManyToOne
    private UserEntity owner;
}
