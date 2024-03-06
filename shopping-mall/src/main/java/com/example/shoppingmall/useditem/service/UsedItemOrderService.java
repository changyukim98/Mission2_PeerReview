package com.example.shoppingmall.useditem.service;

import com.example.shoppingmall.AuthenticationFacade;
import com.example.shoppingmall.useditem.dto.UsedItemOrderDto;
import com.example.shoppingmall.useditem.entity.UsedItemStatus;
import com.example.shoppingmall.useditem.entity.UsedItemOrderStatus;
import com.example.shoppingmall.useditem.entity.UsedItemOrder;
import com.example.shoppingmall.useditem.entity.UsedItem;
import com.example.shoppingmall.useditem.repo.UsedItemOrderRepository;
import com.example.shoppingmall.useditem.repo.UsedItemRepository;
import com.example.shoppingmall.user.entity.UserEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsedItemOrderService {
    private final UsedItemOrderRepository usedItemOrderRepository;
    private final UsedItemRepository itemRepository;
    private final AuthenticationFacade facade;

    public UsedItemOrderDto acceptOrder(Long orderId) {
        Optional<UsedItemOrder> optionalOrder = usedItemOrderRepository.findById(orderId);
        // 구매가 존재하지 않을 경우
        if (optionalOrder.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        UsedItemOrder order = optionalOrder.get();
        UsedItem item = order.getItem();
        UserEntity currentUser = facade.getCurrentUserEntity();

        // 구매의 아이템의 소유자만 수락 가능
        if (!item.getUser().getId().equals(currentUser.getId()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        order.setStatus(UsedItemOrderStatus.ACCEPTED);
        return UsedItemOrderDto.fromEntity(usedItemOrderRepository.save(order));
    }

    public UsedItemOrderDto declineOrder(Long orderId) {
        Optional<UsedItemOrder> optionalOrder = usedItemOrderRepository.findById(orderId);
        // 구매가 존재하지 않을 경우
        if (optionalOrder.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        UsedItemOrder order = optionalOrder.get();
        UsedItem item = order.getItem();
        UserEntity currentUser = facade.getCurrentUserEntity();

        // 구매의 아이템의 소유자만 거절 가능
        if (!item.getUser().getId().equals(currentUser.getId()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        order.setStatus(UsedItemOrderStatus.DECLINED);
        return UsedItemOrderDto.fromEntity(usedItemOrderRepository.save(order));
    }

    @Transactional
    public UsedItemOrderDto confirmAcceptedOrder(Long orderId) {
        Optional<UsedItemOrder> optionalOrder = usedItemOrderRepository.findById(orderId);
        // 구매가 존재하지 않을 경우
        if (optionalOrder.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        UsedItemOrder order = optionalOrder.get();
        UsedItem item = order.getItem();
        UserEntity currentUser = facade.getCurrentUserEntity();

        // 구매의 구매자만 확정 가능
        if (!order.getBuyer().getId().equals(currentUser.getId()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        // 구매의 상태가 ACCEPTED가 아닐 경우
        if (!order.getStatus().equals(UsedItemOrderStatus.ACCEPTED))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        // 아이템의 상태를 판매 완료로
        item.setStatus(UsedItemStatus.SOLD_OUT);
        itemRepository.save(item);

        // 다른 모든 구매를 거절로 만듬
        usedItemOrderRepository.setAllOrdersStatusForItem(UsedItemOrderStatus.DECLINED, item.getId());
        order.setStatus(UsedItemOrderStatus.CONFIRMED);
        return UsedItemOrderDto.fromEntity(usedItemOrderRepository.save(order));
    }
}
