package com.example.shoppingmall.useditem.controller;

import com.example.shoppingmall.useditem.dto.UsedItemOrderDto;
import com.example.shoppingmall.useditem.service.UsedItemOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/used-item/order")
public class UsedItemOrderController {
    private final UsedItemOrderService usedItemOrderService;

    @PostMapping("/{id}/accept")
    public UsedItemOrderDto acceptOrder(
            @PathVariable("id")
            Long orderId
    ) {
        return usedItemOrderService.acceptOrder(orderId);
    }

    @PostMapping("/{id}/decline")
    public UsedItemOrderDto declineOrder(
            @PathVariable("id")
            Long orderId
    ) {
        return usedItemOrderService.declineOrder(orderId);
    }

    @PostMapping("/{id}/confirm")
    public UsedItemOrderDto confirmAcceptedOrder(
            @PathVariable("id")
            Long orderId
    ) {
        return usedItemOrderService.confirmAcceptedOrder(orderId);
    }
}
