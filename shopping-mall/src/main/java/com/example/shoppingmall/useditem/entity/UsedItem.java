package com.example.shoppingmall.useditem.entity;

import com.example.shoppingmall.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsedItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @Enumerated(EnumType.STRING)
    private UsedItemStatus status;

    @ManyToOne
    private UserEntity user;
}
