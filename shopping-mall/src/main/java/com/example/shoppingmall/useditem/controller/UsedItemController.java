package com.example.shoppingmall.useditem.controller;

import com.example.shoppingmall.useditem.dto.UsedItemOrderDto;
import com.example.shoppingmall.useditem.dto.UsedItemDto;
import com.example.shoppingmall.useditem.service.UsedItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/used-item")
public class UsedItemController {
    private final UsedItemService usedItemService;

    @PostMapping
    public UsedItemDto createUsedItem(
            @RequestBody
            UsedItemDto dto
    ) {
        return usedItemService.createUsedItem(dto);
    }

    @GetMapping
    public List<UsedItemDto> readAllItem() {
        return usedItemService.readAllUsedItem();
    }

    @PutMapping("/{id}")
    public UsedItemDto updateUsedItem(
            @PathVariable("id")
            Long id,
            @RequestBody
            UsedItemDto dto
    ) {
        return usedItemService.updateUsedItem(id, dto);
    }

    @DeleteMapping("/{id}")
    public String deleteUsedItem(
            @PathVariable("id")
            Long id
    ) {
        usedItemService.deleteUsedItem(id);
        return "done";
    }

    @PostMapping("/{id}/order")
    public UsedItemOrderDto createOrder(
            @PathVariable("id")
            Long itemId
    ) {
        return usedItemService.createOrder(itemId);
    }

    @GetMapping("/{id}/order")
    public List<UsedItemOrderDto> readOrder(
            @PathVariable("id")
            Long itemId
    ) {
        return usedItemService.readItemOrders(itemId);
    }
}
