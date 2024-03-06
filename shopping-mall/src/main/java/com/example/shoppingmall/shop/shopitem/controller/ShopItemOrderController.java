package com.example.shoppingmall.shop.shopitem.controller;

import com.example.shoppingmall.shop.shopitem.dto.ShopItemOrderResponse;
import com.example.shoppingmall.shop.shopitem.service.ShopItemOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/shop/order")
@RequiredArgsConstructor
public class ShopItemOrderController {
    private final ShopItemOrderService shopItemOrderService;

    @PostMapping("/{orderId}/accept")
    public ShopItemOrderResponse acceptShotItemOrder(
            @PathVariable("orderId")
            Long orderId
    ) {
        return shopItemOrderService.acceptShopItemOrder(orderId);
    }

    @PostMapping("/{orderId}/decline")
    public ShopItemOrderResponse declineShotItemOrder(
            @PathVariable("orderId")
            Long orderId
    ) {
        return shopItemOrderService.declineShopItemOrder(orderId);
    }

    @PostMapping("/{orderId}/cancel")
    public ShopItemOrderResponse cancelShotItemOrder(
            @PathVariable("orderId")
            Long orderId
    ) {
        return shopItemOrderService.cancelShopItemOrder(orderId);
    }
}
