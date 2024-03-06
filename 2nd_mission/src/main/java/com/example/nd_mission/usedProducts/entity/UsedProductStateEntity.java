package com.example.nd_mission.usedProducts.entity;

import lombok.Getter;

@Getter
// 중고 물품 거래간의 상태를 나타내는 Enum
public enum UsedProductStateEntity {
    STATE_ONSALE,       // 판매중
    STATE_SUGGEST,      // 제안 (구매자 측)
    STATE_DECLINE,      // 거절 (판매자 측)
    STATE_ACCEPT,       // 수락 (판매자 측)
    STATE_PURCHASE,     // 구매확정 (구매자 측)
    STATE_SOLD          // 판매 완료
}
