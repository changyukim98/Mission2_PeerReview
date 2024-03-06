package com.example.shoppingmall.shop.shopitem.controller;

import com.example.shoppingmall.shop.shopitem.dto.*;
import com.example.shoppingmall.shop.shopitem.service.ShopItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shop/item")
@RequiredArgsConstructor
public class ShopItemController {
    private final ShopItemService shopItemService;

    @PostMapping
    public ShopItemResponse registerShopItem(
            @RequestPart("image")
            MultipartFile image,
            @RequestPart("dto")
            ShopItemRequest dto
    ) {
        return shopItemService.registerShopItem(image, dto);
    }

    @PutMapping("/{itemId}")
    public ShopItemResponse updateShopItem(
            @PathVariable("itemId")
            Long itemId,
            @RequestPart("image")
            MultipartFile image,
            @RequestPart("dto")
            ShopItemRequest dto
    ) {
        return shopItemService.updateShopItem(itemId, image, dto);
    }

    @DeleteMapping("/{itemId}")
    public String deleteShopItem(
            @PathVariable("itemId")
            Long itemId
    ) {
        shopItemService.deleteShopItem(itemId);
        return "done";
    }

    @PostMapping("/{itemId}/order")
    public ShopItemOrderResponse orderShopItem(
            @PathVariable("itemId")
            Long itemId,
            @RequestBody
            ShopItemOrderRequest request
    ) {
        return shopItemService.orderShopItem(itemId, request);
    }

    @GetMapping("/search")
    public List<ShopItemSearchDto> searchShopItems(
            @RequestParam(value = "nameQ")
            String nameQ,
            @RequestParam(value = "priceMin")
            Long priceMin,
            @RequestParam(value = "priceMax")
            Long priceMax
    ) {
        return shopItemService.searchItems(nameQ, priceMin, priceMax);
    }
}
