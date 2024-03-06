package com.example.shoppingmall.shop.shopitem.service;

import com.example.shoppingmall.AuthenticationFacade;
import com.example.shoppingmall.shop.entity.Shop;
import com.example.shoppingmall.shop.repo.ShopRepository;
import com.example.shoppingmall.shop.shopitem.dto.ShopItemOrderRequest;
import com.example.shoppingmall.shop.shopitem.dto.ShopItemOrderResponse;
import com.example.shoppingmall.shop.shopitem.entity.ShopItem;
import com.example.shoppingmall.shop.shopitem.entity.ShopItemOrder;
import com.example.shoppingmall.shop.shopitem.entity.ShopItemOrderStatus;
import com.example.shoppingmall.shop.shopitem.repo.ShopItemOrderRepository;
import com.example.shoppingmall.shop.shopitem.repo.ShopItemRepository;
import com.example.shoppingmall.user.entity.UserEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopItemOrderService {
    private final ShopItemOrderRepository shopItemOrderRepository;
    private final ShopItemRepository shopItemRepository;
    private final ShopRepository shopRepository;
    private final AuthenticationFacade facade;

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
}
