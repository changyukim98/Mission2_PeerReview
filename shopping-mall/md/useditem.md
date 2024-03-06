# 중고거래 중개하기

## 중고 물품 등록
```Java
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

```

```Java
    public UsedItemDto createUsedItem(UsedItemDto dto) {
        UserEntity currentUser = facade.getCurrentUserEntity();

        // 일반 사용자의 경우에만 아이템 등록 가능
        if (!currentUser.getRole().equals(UserRole.ROLE_USER))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        UsedItem usedItem = UsedItem.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .user(currentUser)
                .status(UsedItemStatus.ON_SALE)
                .build();
        return UsedItemDto.fromEntity(usedItemRepository.save(usedItem));
    }
```
> 일반 사용자의 한해서만 UsedItemDto로 아이템 정보를 전달받아 UsedItem을 생성해 저장할 수 있도록 한다. 초기 상태는 ON_SALE 상태이다.

## 중고 물품 열람
```Java
    public List<UsedItemDto> readAllUsedItem() {
        UserEntity currentUser = facade.getCurrentUserEntity();

        if (currentUser.getRole().equals(UserRole.ROLE_INACTIVE))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        return usedItemRepository.findAll().stream()
                .map(UsedItemDto::fromEntity)
                .toList();
    }
```
> usedItemRepository로부터 모든 아이템을 찾아 UsedItemDto로 변환해 반환한다.

## 중고 물품 수정/삭제
```Java
public UsedItemDto updateUsedItem(Long id, UsedItemDto dto) {
        Optional<UsedItem> optionalItem = usedItemRepository.findById(id);
        if (optionalItem.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        UsedItem usedItem = optionalItem.get();
        UserEntity currentUser = facade.getCurrentUserEntity();
        if (!usedItem.getUser().getId().equals(currentUser.getId()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        usedItem.setTitle(dto.getTitle());
        usedItem.setDescription(dto.getDescription());
        usedItem.setPrice(dto.getPrice());
        return UsedItemDto.fromEntity(usedItemRepository.save(usedItem));
    }

    public void deleteUsedItem(Long id) {
        Optional<UsedItem> optionalItem = usedItemRepository.findById(id);
        if (optionalItem.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        UsedItem usedItem = optionalItem.get();
        UserEntity currentUser = facade.getCurrentUserEntity();
        if (!usedItem.getUser().getUsername().equals(currentUser.getUsername()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        usedItemRepository.deleteById(id);
    }
```
> 현재 접속 유저가 물품 등록자인지 확인이 된다면 삭제, 수정이 가능하다.

## 구매 제안 등록
```Java
    public UsedItemOrderDto createOrder(Long itemId) {
        Optional<UsedItem> optionalItem = usedItemRepository.findById(itemId);
        // item이 존재하지 않을 경우
        if (optionalItem.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        UsedItem usedItem = optionalItem.get();
        // 판매 완료일시 새로운 구매 불가
        if (usedItem.getStatus().equals(UsedItemStatus.SOLD_OUT))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        UserEntity currentUser = facade.getCurrentUserEntity();
        // 비활성회원은 구매 불가
        if (currentUser.getRole().equals(UserRole.ROLE_INACTIVE))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        // 등록자와 구매자가 같은 경우 구매 불가
        if (usedItem.getUser().getId().equals(currentUser.getId()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        UsedItemOrder order = UsedItemOrder.builder()
                .item(usedItem)
                .buyer(currentUser)
                .status(UsedItemOrderStatus.WAITING)
                .build();

        return UsedItemOrderDto.fromEntity(usedItemOrderRepository.save(order));
    }
```
> 특정 물품에 대해 구매 제안을 등록한다.  
status를 WAITING으로 초기 설정해 Order Entity를 만든다.

## 구매 제안 조회
```Java
    public List<UsedItemOrderDto> readItemOrders(Long itemId) {
        Optional<UsedItem> optionalItem = usedItemRepository.findById(itemId);
        // 아이템이 존재하지 않는 경우
        if (optionalItem.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        // 비활성회원은 조회 불가
        UserEntity currentUser = facade.getCurrentUserEntity();
        if (currentUser.getRole().equals(UserRole.ROLE_INACTIVE))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        UsedItem usedItem = optionalItem.get();

        // 조회자가 아이템의 소유자인 경우
        if (usedItem.getUser().getId().equals(currentUser.getId())) {
            return usedItemOrderRepository.findAllByItemId(itemId).stream()
                    .map(UsedItemOrderDto::fromEntity)
                    .toList();
        } else {
            return usedItemOrderRepository.findAllByItemIdAndBuyerId(itemId, currentUser.getId())
                    .stream()
                    .map(UsedItemOrderDto::fromEntity)
                    .toList();
        }
    }
```

> 특정 물품에 대해 걸린 구매 제안을 조회한다.  
현재 접속 유저가 소유자일 경우 모든 구매 제안을, 그 외의 경우에는 자신이 건 구매 제안만 확인한다.

## 구매 제안 수락/거절
```Java
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
```
> 자신의 물품에 걸린 구매 제안을 수락/거절한다. order의 status가 변화한다.

## 구매 확정
```Java
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
```
> 수락된 자신의 구매 제안에 대해 구매 제안을 확정한다.  
자신의 구매제안은 status가 CONFIRMED가 되며, 다른 모든 제안은 DECLINED가 된다.



