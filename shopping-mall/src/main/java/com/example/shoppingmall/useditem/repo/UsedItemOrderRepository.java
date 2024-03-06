package com.example.shoppingmall.useditem.repo;

import com.example.shoppingmall.useditem.entity.UsedItemOrderStatus;
import com.example.shoppingmall.useditem.entity.UsedItemOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UsedItemOrderRepository
        extends JpaRepository<UsedItemOrder, Long> {

    // 소유자가 자신의 아이템에 걸린 구매를 확인하는 메서드
    List<UsedItemOrder> findAllByItemId(Long itemId);

    // 구매자가 자신의 구매를 확인하는 메서드
    List<UsedItemOrder> findAllByItemIdAndBuyerId(Long itemId, Long buyerId);

    @Modifying
    @Query("UPDATE UsedItemOrder o " +
            "SET o.status = :status " +
            "WHERE o.item.id = :itemId")
    void setAllOrdersStatusForItem(
            @Param("status") UsedItemOrderStatus status,
            @Param("itemId") Long itemId
    );
}
