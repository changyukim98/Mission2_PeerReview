package com.example.shoppingmall.shop.controller;

import com.example.shoppingmall.shop.dto.*;
import com.example.shoppingmall.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("shop")
public class ShopController {
    private final ShopService shopService;

    @PostMapping("regs")
    public ShopRegResponseDto registerShop(
            @RequestBody
            ShopRegDto dto
    ) {
        return shopService.registerShop(dto);
    }

    @GetMapping("regs")
    public List<ShopRegResponseDto> readAllShopReg() {
        return shopService.readAllShopReg();
    }

    @PostMapping("regs/{regId}/accept")
    public ShopDto acceptShopReg(
            @PathVariable("regId")
            Long regId
    ) {
        return shopService.acceptShopReg(regId);
    }

    @PostMapping("regs/{regId}/decline")
    public ShopRegResponseDto declineShopReg(
            @PathVariable("regId")
            Long regId,
            @RequestBody
            ShopRegDeclineDto dto
    ) {
        return shopService.declineShopReg(regId, dto);
    }

    @PostMapping("close")
    public ShopCloseResponseDto shopCloseRequest(
            @RequestBody
            ShopCloseRequestDto dto
    ) {
        return shopService.shopCloseRequest(dto);
    }

    @GetMapping("close")
    public List<ShopCloseResponseDto> readAllCloseRequest() {
        return shopService.readAllCloseRequest();
    }

    @PostMapping("close/{reqId}")
    public ShopDto closeShop(
            @PathVariable("reqId")
            Long reqId
    ) {
        return shopService.closeShop(reqId);
    }

    @GetMapping("/search")
    public List<ShopDto> searchShops(
            @RequestParam(value = "nameQ", required = false)
            String nameQ,
            @RequestParam(value = "category", required = false)
            String category
    ) {
        return shopService.searchShops(nameQ, category);
    }

}
