package com.example.nd_mission.usedProducts.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("suggestions")
@RequiredArgsConstructor
public class SuggestionController {
    // 어떤 아이템에 대한 정보 확인
    // 판매자 혹은 제안자에 대한 경우
    @GetMapping("/{id}")
    public void checkSuggetstionDetails() {

    }

    // 어떤 아이템에 대해 판매자가 제안을 수락함
    @PutMapping("/{id}/accept")
    public void SellerAcceptSuggestion() {

    }
    // 어떤 아이템에 대해 판매자가 제안을 거절함
    @PutMapping("/{id}/decline")
    public void SellerDeclineSuggetstion() {

    }
    // 어떤 아이템에 대해 판매자가 수락한 후
    // 구매자가 구매를 확정함
    @PutMapping("/{id}/confirm")
    public void BuyerAcceptAndConfirm() {

    }
}
