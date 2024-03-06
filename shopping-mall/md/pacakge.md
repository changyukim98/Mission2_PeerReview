# 패키지 구조
```Text
├─media
│  ├─captcha
│  ├─item
│  └─profile
└─src
    ├─main
    │  ├─java
    │  │  └─com
    │  │      └─example
    │  │          └─shoppingmall
    │  │              │  AuthenticationFacade.java
    │  │              │  ShoppingMallApplication.java
    │  │              │
    │  │              ├─api
    │  │              │  └─captcha
    │  │              │          NcpCaptchaApiService.java
    │  │              │          NcpCaptchaVerifyResponse.java
    │  │              │          NcpClientConfig.java
    │  │              │
    │  │              ├─config
    │  │              │      PasswordEncoderConfig.java
    │  │              │      UserConfig.java
    │  │              │      WebSecurityConfig.java
    │  │              │
    │  │              ├─jwt
    │  │              │      JwtResponseDto.java
    │  │              │      JwtTokenFilter.java
    │  │              │      JwtTokenUtils.java
    │  │              │
    │  │              ├─shop
    │  │              │  ├─controller
    │  │              │  │      ShopController.java
    │  │              │  │
    │  │              │  ├─dto
    │  │              │  │      ShopCloseRequestDto.java
    │  │              │  │      ShopCloseResponseDto.java
    │  │              │  │      ShopDto.java
    │  │              │  │      ShopRegDeclineDto.java
    │  │              │  │      ShopRegDto.java
    │  │              │  │      ShopRegResponseDto.java
    │  │              │  │
    │  │              │  ├─entity
    │  │              │  │      Shop.java
    │  │              │  │      ShopCategory.java
    │  │              │  │      ShopCloseRequest.java
    │  │              │  │      ShopRegistration.java
    │  │              │  │      ShopRegStatus.java
    │  │              │  │      ShopStatus.java
    │  │              │  │
    │  │              │  ├─repo
    │  │              │  │      CloseRequestRepository.java
    │  │              │  │      ShopRegRepository.java
    │  │              │  │      ShopRepository.java
    │  │              │  │
    │  │              │  ├─service
    │  │              │  │      ShopService.java
    │  │              │  │
    │  │              │  └─shopitem
    │  │              │      ├─controller
    │  │              │      │      ShopItemController.java
    │  │              │      │      ShopItemOrderController.java
    │  │              │      │
    │  │              │      ├─dto
    │  │              │      │      ShopItemOrderRequest.java
    │  │              │      │      ShopItemOrderResponse.java
    │  │              │      │      ShopItemRequest.java
    │  │              │      │      ShopItemResponse.java
    │  │              │      │      ShopItemSearchDto.java
    │  │              │      │
    │  │              │      ├─entity
    │  │              │      │      ShopItem.java
    │  │              │      │      ShopItemOrder.java
    │  │              │      │      ShopItemOrderStatus.java
    │  │              │      │
    │  │              │      ├─repo
    │  │              │      │      ShopItemOrderRepository.java
    │  │              │      │      ShopItemRepository.java
    │  │              │      │
    │  │              │      └─service
    │  │              │              ShopItemOrderService.java
    │  │              │              ShopItemService.java
    │  │              │
    │  │              ├─useditem
    │  │              │  ├─controller
    │  │              │  │      UsedItemController.java
    │  │              │  │      UsedItemOrderController.java
    │  │              │  │
    │  │              │  ├─dto
    │  │              │  │      UsedItemDto.java
    │  │              │  │      UsedItemOrderDto.java
    │  │              │  │
    │  │              │  ├─entity
    │  │              │  │      UsedItem.java
    │  │              │  │      UsedItemOrder.java
    │  │              │  │      UsedItemOrderStatus.java
    │  │              │  │      UsedItemStatus.java
    │  │              │  │
    │  │              │  ├─repo
    │  │              │  │      UsedItemOrderRepository.java
    │  │              │  │      UsedItemRepository.java
    │  │              │  │
    │  │              │  └─service
    │  │              │          UsedItemOrderService.java
    │  │              │          UsedItemService.java
    │  │              │
    │  │              └─user
    │  │                  ├─controller
    │  │                  │      UserController.java
    │  │                  │
    │  │                  ├─dto
    │  │                  │      BusinessRegDto.java
    │  │                  │      EssentialInfoDto.java
    │  │                  │      LoginDto.java
    │  │                  │      RegisterDto.java
    │  │                  │      UserDto.java
    │  │                  │
    │  │                  ├─entity
    │  │                  │      BusinessRegistration.java
    │  │                  │      LoginAttempt.java
    │  │                  │      UserEntity.java
    │  │                  │      UserRole.java
    │  │                  │
    │  │                  ├─repo
    │  │                  │      BusinessRepository.java
    │  │                  │      LoginAttemptRepository.java
    │  │                  │      UserRepository.java
    │  │                  │
    │  │                  └─service
    │  │                          UserService.java
    │  │
    │  └─resources
    │      │  application-ncp.yaml
    │      │  application.yaml
    │      │
    │      ├─static
    │      └─templates
    └─test
        └─java
            └─com
                └─example
                    └─shoppingmall
                            ShoppingMallApplicationTests.java
```
