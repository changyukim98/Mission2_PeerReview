package com.example.shoppingmall.shop.shopitem.service;

import com.example.shoppingmall.AuthenticationFacade;
import com.example.shoppingmall.shop.entity.Shop;
import com.example.shoppingmall.shop.repo.ShopRepository;
import com.example.shoppingmall.shop.shopitem.dto.*;
import com.example.shoppingmall.shop.shopitem.entity.ShopItem;
import com.example.shoppingmall.shop.shopitem.entity.ShopItemOrder;
import com.example.shoppingmall.shop.shopitem.entity.ShopItemOrderStatus;
import com.example.shoppingmall.shop.shopitem.repo.ShopItemOrderRepository;
import com.example.shoppingmall.shop.shopitem.repo.ShopItemRepository;
import com.example.shoppingmall.user.entity.UserEntity;
import com.example.shoppingmall.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopItemService {
    private final ShopItemRepository shopItemRepository;
    private final ShopRepository shopRepository;
    private final ShopItemOrderRepository shopItemOrderRepository;
    private final AuthenticationFacade facade;

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

    public String saveItemImage(MultipartFile image) {
        String itemImageDir = "media/item/";

        // 폴더 만들기
        try {
            Files.createDirectories(Path.of(itemImageDir));
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 파일 이름
        String originalFilename = image.getOriginalFilename();
        String imageFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        // 파일 경로
        String imagePath = itemImageDir + imageFilename;
        // 저장
        try {
            image.transferTo(Path.of(imagePath));
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return imagePath;
    }

    public void deleteFile(String filePath) {
        try {
            Files.delete(Path.of(filePath));
        } catch (IOException e) {
            log.error(e.toString());
            throw new RuntimeException(e);
        }
    }
}
