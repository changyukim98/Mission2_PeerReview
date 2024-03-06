package com.example.shoppingmall.user.dto;

import com.example.shoppingmall.user.entity.BusinessRegistration;
import com.example.shoppingmall.user.entity.UserEntity;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessRegDto {
    private Long id;
    @Setter
    private Long userId;
    @Setter
    private String businessNum;

    public static BusinessRegDto fromEntity(BusinessRegistration entity) {
        return BusinessRegDto.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .businessNum(entity.getBusinessNum())
                .build();
    }
}
