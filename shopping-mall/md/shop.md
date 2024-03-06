# 쇼핑몰 운영하기

## 쇼핑몰 등록 신청
```Java
package com.example.shoppingmall.shop.entity;

import com.example.shoppingmall.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shop {
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
    private ShopStatus status;
    @Setter
    private LocalTime lastPurchased;

    @OneToOne
    private UserEntity owner;
}
```

```java
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

```

```Java
    public ShopRegResponseDto registerShop(ShopRegDto dto) {
        UserEntity currentUser = facade.getCurrentUserEntity();

        // 사업자 사용자만 등록 가능
        if (!currentUser.getRole().equals(UserRole.ROLE_BUSINESS))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Optional<Shop> optionalShop = shopRepository.findByOwnerId(currentUser.getId());
        if (optionalShop.isEmpty())
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        Shop shop = optionalShop.get();

        // 이미 오픈한 상점이라면 등록 불가능
        if (shop.getStatus().equals(ShopStatus.OPEN))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        // 이미 대기중인 등록이 있다면 등록 불가능
        if (shopRegRepository.existsByOwnerIdAndStatus(
                currentUser.getId(),
                ShopRegStatus.WAITING))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        ShopRegistration shopReg = ShopRegistration.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .owner(currentUser)
                .status(ShopRegStatus.WAITING)
                .build();
        return ShopRegResponseDto.fromEntity(shopRegRepository.save(shopReg));
    }
```
> ShopRegDto를 입력받아 ShopRegistration Entity를 만들어 저장한다.
사업자 사용자의 중복되지 않은 신청에 대해서만 진행할 수 있도록 한다.

## 쇼핑몰 등록 신청 조회/허가/거절
```Java
    public List<ShopRegResponseDto> readAllShopReg() {
        UserEntity currentUser = facade.getCurrentUserEntity();

        // admin만 조회 가능
        if (currentUser.getRole().equals(UserRole.ROLE_ADMIN)) {
            return shopRegRepository.findAll().stream()
                    .map(ShopRegResponseDto::fromEntity)
                    .toList();
        } else if (currentUser.getRole().equals(UserRole.ROLE_BUSINESS)) {
            return shopRegRepository.findAllByOwnerId(currentUser.getId())
                    .stream()
                    .map(ShopRegResponseDto::fromEntity)
                    .toList();
        } else
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

    @Transactional
    public ShopDto acceptShopReg(Long regId) {
        UserEntity currentUser = facade.getCurrentUserEntity();
        // admin만 허가 가능
        if (!currentUser.getRole().equals(UserRole.ROLE_ADMIN))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Optional<ShopRegistration> optionalReg = shopRegRepository.findById(regId);
        // 등록 신청이 없는 경우
        if (optionalReg.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        ShopRegistration shopReg = optionalReg.get();
        // 이미 처리된 Shop 등록 요청인 경우
        if (!shopReg.getStatus().equals(ShopRegStatus.WAITING))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        UserEntity owner = shopReg.getOwner();
        Optional<Shop> optionalShop = shopRepository.findByOwnerId(owner.getId());
        // 행여나 Shop이 존재하지 않을 경우
        if (optionalShop.isEmpty())
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);

        shopReg.setStatus(ShopRegStatus.ACCEPTED);
        shopRegRepository.save(shopReg);

        Shop shop = optionalShop.get();
        shop.setName(shopReg.getName());
        shop.setDescription(shopReg.getDescription());
        shop.setCategory(shopReg.getCategory());
        shop.setStatus(ShopStatus.OPEN);

        return ShopDto.fromEntity(shopRepository.save(shop));
    }

    public ShopRegResponseDto declineShopReg(Long regId, ShopRegDeclineDto dto) {
        UserEntity currentUser = facade.getCurrentUserEntity();
        // admin만 허가 가능
        if (!currentUser.getRole().equals(UserRole.ROLE_ADMIN))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Optional<ShopRegistration> optionalReg = shopRegRepository.findById(regId);
        // 등록 신청이 없는 경우
        if (optionalReg.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        ShopRegistration shopReg = optionalReg.get();
        // 이미 처리된 Shop 등록 요청인 경우
        if (!shopReg.getStatus().equals(ShopRegStatus.WAITING))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        shopReg.setStatus(ShopRegStatus.DECLINED);
        shopReg.setDeclineReason(dto.getReason());
        return ShopRegResponseDto.fromEntity(shopRegRepository.save(shopReg));
    }
```
> Admin 유저에 한해 ShopReg를 조회하고 ShopReg의 아이디를 이용해 허가 및 거절을 진행할 수 있다. 단, 거절할 경우에는 declineReason을 작성한다.

## 쇼핑몰 폐쇄 신청
```Java
    public ShopCloseResponseDto shopCloseRequest(ShopCloseRequestDto dto) {
        UserEntity currentUser = facade.getCurrentUserEntity();

        // 사업자 사용자만 폐쇄 신청 가능
        if (!currentUser.getRole().equals(UserRole.ROLE_BUSINESS))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Optional<Shop> optionalShop = shopRepository.findByOwnerId(currentUser.getId());
        if (optionalShop.isEmpty())
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        Shop shop = optionalShop.get();

        // 이미 신청한 요청인지 확인
        if (closeRequestRepository.existsByOwnerId(currentUser.getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        // 오픈중인 Shop만 Close 가능
        if (!shop.getStatus().equals(ShopStatus.OPEN))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        ShopCloseRequest closeRequest = ShopCloseRequest.builder()
                .owner(currentUser)
                .reason(dto.getReason())
                .build();
        return ShopCloseResponseDto.fromEntity(closeRequestRepository.save(closeRequest));
    }
```
> 사업자 유저의 오픈한 상점에 대한 중복 신청이 아닌 경우에만 폐쇄 신청을 진행한다.

## 쇼핑몰 폐쇄 조회/폐쇄 진행
```Java
    public List<ShopCloseResponseDto> readAllCloseRequest() {
        UserEntity currentUser = facade.getCurrentUserEntity();

        // admin만 조회 가능
        if (!currentUser.getRole().equals(UserRole.ROLE_ADMIN))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        return closeRequestRepository.findAll().stream()
                .map(ShopCloseResponseDto::fromEntity)
                .toList();
    }

    @Transactional
    public ShopDto closeShop(Long reqId) {
        UserEntity currentUser = facade.getCurrentUserEntity();

        // admin만 조회 가능
        if (!currentUser.getRole().equals(UserRole.ROLE_ADMIN))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Optional<ShopCloseRequest> optionalRequest = closeRequestRepository.findById(reqId);
        // request가 존재하지 않는 경우 BAD_REQUEST
        if (optionalRequest.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        ShopCloseRequest request = optionalRequest.get();
        UserEntity owner = request.getOwner();

        Optional<Shop> optionalShop = shopRepository.findByOwnerId(owner.getId());
        // owner의 shop이 없는 경우 에러
        if (optionalShop.isEmpty())
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);

        closeRequestRepository.delete(request);

        Shop shop = optionalShop.get();
        shop.setStatus(ShopStatus.CLOSED);
        return ShopDto.fromEntity(shopRepository.save(shop));
    }
```
> Admin 유저는 폐쇄 신청을 조회하고, 해당 폐쇄 신청 id를 통해, 폐쇄를 진행할 수 있다.

## 쇼핑몰 아이템 등록/수정/삭제
```Java
    public ShopItemResponse registerShopItem(
            MultipartFile image,
            ShopItemRequest itemDto
    ) {
        UserEntity currentUser = facade.getCurrentUserEntity();

        // 비지니스 사용자만 아이템 등록가능
        if (!currentUser.getRole().equals(UserRole.ROLE_BUSINESS))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Optional<Shop> optionalShop = shopRepository.findByOwnerId(currentUser.getId());
        if (optionalShop.isEmpty())
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        Shop shop = optionalShop.get();

        // 이미지 저장
        String imagePath = saveItemImage(image);

        ShopItem shopItem = ShopItem.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .price(itemDto.getPrice())
                .imagePath(imagePath)
                .stock(itemDto.getStock())
                .shop(shop)
                .build();

        return ShopItemResponse.fromEntity(shopItemRepository.save(shopItem));
    }

    public ShopItemResponse updateShopItem(
            Long itemId,
            MultipartFile image,
            ShopItemRequest itemDto
    ) {
        UserEntity currentUser = facade.getCurrentUserEntity();

        // 비지니스 사용자만 아이템 수정 가능
        if (!currentUser.getRole().equals(UserRole.ROLE_BUSINESS))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Optional<ShopItem> optionalItem = shopItemRepository.findById(itemId);
        // 존재하지 않는 item일 경우 잘못된 요청
        if (optionalItem.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        ShopItem shopItem = optionalItem.get();

        Optional<Shop> optionalShop = shopRepository.findByOwnerId(currentUser.getId());
        // shop이 존재하지 않으면 에러
        if (optionalShop.isEmpty())
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        Shop shop = optionalShop.get();

        // 자신의 상점의 상품이 아닐 경우
        if (!shopItem.getShop().getId().equals(shop.getId()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        // 기존 이미지 삭제
        deleteFile(shopItem.getImagePath());
        // 새 이미지 저장
        String imagePath = saveItemImage(image);

        shopItem.setName(itemDto.getName());
        shopItem.setDescription(itemDto.getDescription());
        shopItem.setPrice(itemDto.getPrice());
        shopItem.setImagePath(imagePath);
        shopItem.setStock(itemDto.getStock());

        return ShopItemResponse.fromEntity(shopItemRepository.save(shopItem));
    }

    public void deleteShopItem(Long itemId) {
        UserEntity currentUser = facade.getCurrentUserEntity();

        // 비지니스 사용자만 아이템 삭제 가능
        if (!currentUser.getRole().equals(UserRole.ROLE_BUSINESS))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Optional<ShopItem> optionalItem = shopItemRepository.findById(itemId);
        // 존재하지 않는 item일 경우 잘못된 요청
        if (optionalItem.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        ShopItem shopItem = optionalItem.get();

        Optional<Shop> optionalShop = shopRepository.findByOwnerId(currentUser.getId());
        // shop이 존재하지 않으면 에러
        if (optionalShop.isEmpty())
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        Shop shop = optionalShop.get();

        // 자신의 상점의 상품이 아닐 경우
        if (!shopItem.getShop().getId().equals(shop.getId()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        // 이미지 삭제
        deleteFile(shopItem.getImagePath());

        shopItemRepository.deleteById(itemId);
    }
```
> 비지니스 사용자는 자신의 오픈된 상점에 한해, 물품을 등록/수정/삭제를 진행할 수 있다.



## 쇼핑몰 조회
```Java
    public List<ShopDto> searchShops(String nameQ, String category) {
        UserEntity currentUser = facade.getCurrentUserEntity();
        // 비활성사용자는 조회불가
        if (currentUser.getRole().equals(UserRole.ROLE_INACTIVE))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        List<Shop> shopList;
        if (nameQ == null && category == null) {
            // 검색 조건이 없는 경우
            shopList = shopRepository.findAllByOrderByLastPurchasedDesc();
        }
        if (nameQ != null && category == null) {
            // 이름만 가지고 검색
            shopList = shopRepository.findAllByNameContaining(nameQ);
        } else if (nameQ == null && category != null) {
            // 카테고리만으로 검색
            try {
                shopList = shopRepository.findAllByCategory(
                        ShopCategory.valueOf(category));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        } else {
            try {
                shopList = shopRepository.findAllByNameContainingAndCategory(
                        nameQ, ShopCategory.valueOf(category));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        }

        return shopList.stream().map(ShopDto::fromEntity).toList();
    }
```
> 쇼핑몰 이름에 대한 쿼리 nameQ와 카테고리를 입력받아 검색 조건이 없는 경우, 가장 최근에 구매가 진행된 상점 순으로 검색한다.  
nameQ와 category의 null 여부에 따라 검색 결과를 달리한다.

## 쇼핑몰 상품 검색
```Java
    public List<ShopItemSearchDto> searchItems(String nameQ, Long priceMin, Long priceMax) {
        UserEntity currentUser = facade.getCurrentUserEntity();
        // 비활성사용자는 조회불가
        if (currentUser.getRole().equals(UserRole.ROLE_INACTIVE))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        if (priceMin > priceMax)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        List<ShopItem> itemList
                = shopItemRepository.searchShopItems(nameQ, priceMin, priceMax);
        return itemList.stream().map(ShopItemSearchDto::fromEntity).toList();
    }
```
> 상품의 이름 쿼리와, 가격의 최대 최소를 입력받아 상품 쿼리를 진행한다.   
ShopItemSearchDto에는 해당 아이템의 상점 정보도 포함하도록 한다.

## 쇼핑몰 상품 구매 요청
```Java
    public ShopItemOrderResponse orderShopItem(
            Long itemId,
            ShopItemOrderRequest request
    ) {
        UserEntity currentUser = facade.getCurrentUserEntity();

        // 비활성 사용자는 구매 불가
        if (currentUser.getRole().equals(UserRole.ROLE_INACTIVE))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Optional<ShopItem> optionalItem = shopItemRepository.findById(itemId);
        // 아이템이 존재하지 않을 시
        if (optionalItem.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        ShopItem shopItem = optionalItem.get();

        ShopItemOrder order = ShopItemOrder.builder()
                .shopItem(shopItem)
                .customer(currentUser)
                .quantity(request.getQuantity())
                .status(ShopItemOrderStatus.WAITING)
                .build();

        return ShopItemOrderResponse.fromEntity(shopItemOrderRepository.save(order));
    }
```
> ShopItemOrderRequest라는 Dto로부터 아이템 id와, 수량을 받아 order를 진행한다.  
ShopItemOrder Entity를 만들어 저장한다. 초기 status는 WAITING이다.

## 쇼핑몰 상품 구매 수락/거절
```Java
    @Transactional
    public ShopItemOrderResponse acceptShopItemOrder(Long orderId) {
        UserEntity currentUser = facade.getCurrentUserEntity();

        Optional<ShopItemOrder> optionalOrder = shopItemOrderRepository.findById(orderId);
        if (optionalOrder.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        ShopItemOrder order = optionalOrder.get();

        ShopItem shopItem = order.getShopItem();
        Shop shop = shopItem.getShop();
        UserEntity owner = shop.getOwner();

        // 상점의 소유자가 아닐 경우 구매 허가를 하지 못함
        if (!owner.getId().equals(currentUser.getId()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        // 이미 처리된 주문인 경우
        if (!order.getStatus().equals(ShopItemOrderStatus.WAITING))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        // 수량이 부족한 경우
        if (shopItem.getStock() < order.getQuantity())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        shopItem.setStock(shopItem.getStock() - order.getQuantity());
        shopItemRepository.save(shopItem);

        shop.setLastPurchased(LocalTime.now());
        shopRepository.save(shop);

        order.setStatus(ShopItemOrderStatus.ACCEPTED);
        return ShopItemOrderResponse.fromEntity(shopItemOrderRepository.save(order));
    }

    public ShopItemOrderResponse declineShopItemOrder(Long orderId) {
        UserEntity currentUser = facade.getCurrentUserEntity();

        Optional<ShopItemOrder> optionalOrder = shopItemOrderRepository.findById(orderId);
        if (optionalOrder.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        ShopItemOrder order = optionalOrder.get();

        ShopItem shopItem = order.getShopItem();
        Shop shop = shopItem.getShop();
        UserEntity owner = shop.getOwner();

        // 상점의 소유자가 아닐 경우 구매 거부를 하지 못함
        if (!owner.getId().equals(currentUser.getId()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        // 이미 처리된 주문인 경우
        if (!order.getStatus().equals(ShopItemOrderStatus.WAITING))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        order.setStatus(ShopItemOrderStatus.DECLINED);
        return ShopItemOrderResponse.fromEntity(shopItemOrderRepository.save(order));
    }
```
> 쇼핑몰 주인에 한해, 자신의 상품에 걸린 상품 구매를 수락, 거절을 진행할 수 있다.  
결과로 status가 ACCEPTED 또는 DECLINED로 변화한다.

## 쇼핑몰 상품 구매 취소
```Java
public ShopItemOrderResponse cancelShopItemOrder(Long orderId) {
        UserEntity currentUser = facade.getCurrentUserEntity();

        Optional<ShopItemOrder> optionalOrder = shopItemOrderRepository.findById(orderId);
        if (optionalOrder.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        ShopItemOrder order = optionalOrder.get();

        // 본인의 주문만 처리 가능함
        if (!order.getCustomer().getId().equals(currentUser.getId()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        // 이미 처리된 주문인 경우
        if (!order.getStatus().equals(ShopItemOrderStatus.WAITING))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        order.setStatus(ShopItemOrderStatus.CANCELED);
        return ShopItemOrderResponse.fromEntity(shopItemOrderRepository.save(order));
    }
```
> 상품 구매자에 한해 구매가 수락되기전인 WAITING 상태일 경우, 구매 취소를 진행할 수 있다. 상품의 상태가 CANCELD로 변화한다.