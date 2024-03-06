package com.example.nd_mission.usedProducts.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("items")
@RequiredArgsConstructor
public class UsedProductController {

    // 중고 아이템 등록
    @PostMapping("/register")
    public void productRegister() {

    }

    // 중고 아이템들 조회
    @GetMapping("/register")
    public void productLists() {

    }

    // 중고 아이템 세부 내용 확인
    @GetMapping("/{id}")
    public void productDetails(
            @PathVariable("id")
            Long id
    ) {

    }

    // 중고 아이템 내용 업데이트 (판매자가)
    @PutMapping("/{id}")
    public void productUpdate(
            @PathVariable("id")
            Long id
    ) {

    }
    // 판매자가 중고 아이템 등록 삭제
    @DeleteMapping("/{id}")
    public void productDelete(
            @PathVariable("id")
            Long id
    ) {

    }
    // 구매자가 구매 제안
    @PostMapping("/{id}/suggestion")
    public void purchaseSuggetstion() {

    }
    // 판매자가 구매 제안들 확인
    @GetMapping("/{id}/suggestion")
    public void checkSuggetstion() {

    }
}
