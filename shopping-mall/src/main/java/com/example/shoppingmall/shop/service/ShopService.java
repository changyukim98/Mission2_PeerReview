package com.example.shoppingmall.shop.service;

import com.example.shoppingmall.AuthenticationFacade;
import com.example.shoppingmall.shop.dto.*;
import com.example.shoppingmall.shop.entity.*;
import com.example.shoppingmall.shop.repo.CloseRequestRepository;
import com.example.shoppingmall.shop.repo.ShopRegRepository;
import com.example.shoppingmall.shop.repo.ShopRepository;
import com.example.shoppingmall.user.entity.UserEntity;
import com.example.shoppingmall.user.entity.UserRole;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopService {
    private final ShopRegRepository shopRegRepository;
    private final ShopRepository shopRepository;
    private final CloseRequestRepository closeRequestRepository;
    private final AuthenticationFacade facade;

    public ShopRegResponseDto registerShop(ShopRegDto dto) {
        UserEntity currentUser = facade.getCurrentUserEntity();

        // 사업자 사용자만 등록 가능
        if (!currentUser.getRole().equals(UserRole.ROLE_BUSINESS))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Optional<Shop> optionalShop = shopRepository.findByOwnerId(currentUser.getId());
        if (optionalShop.isEmpty())
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        Shop shop = optionalShop.get();

        // 이미 오픈한 상점이라면 등록 불가능
        if (shop.getStatus().equals(ShopStatus.OPEN))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        // 이미 대기중인 등록이 있다면 등록 불가능
        if (shopRegRepository.existsByOwnerIdAndStatus(
                currentUser.getId(),
                ShopRegStatus.WAITING))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        ShopRegistration shopReg = ShopRegistration.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .owner(currentUser)
                .status(ShopRegStatus.WAITING)
                .build();
        return ShopRegResponseDto.fromEntity(shopRegRepository.save(shopReg));
    }

    public List<ShopRegResponseDto> readAllShopReg() {
        UserEntity currentUser = facade.getCurrentUserEntity();

        // admin만 조회 가능
        if (currentUser.getRole().equals(UserRole.ROLE_ADMIN)) {
            return shopRegRepository.findAll().stream()
                    .map(ShopRegResponseDto::fromEntity)
                    .toList();
        } else if (currentUser.getRole().equals(UserRole.ROLE_BUSINESS)) {
            return shopRegRepository.findAllByOwnerId(currentUser.getId())
                    .stream()
                    .map(ShopRegResponseDto::fromEntity)
                    .toList();
        } else
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

    @Transactional
    public ShopDto acceptShopReg(Long regId) {
        UserEntity currentUser = facade.getCurrentUserEntity();
        // admin만 허가 가능
        if (!currentUser.getRole().equals(UserRole.ROLE_ADMIN))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Optional<ShopRegistration> optionalReg = shopRegRepository.findById(regId);
        // 등록 신청이 없는 경우
        if (optionalReg.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        ShopRegistration shopReg = optionalReg.get();
        // 이미 처리된 Shop 등록 요청인 경우
        if (!shopReg.getStatus().equals(ShopRegStatus.WAITING))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        UserEntity owner = shopReg.getOwner();
        Optional<Shop> optionalShop = shopRepository.findByOwnerId(owner.getId());
        // 행여나 Shop이 존재하지 않을 경우
        if (optionalShop.isEmpty())
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);

        shopReg.setStatus(ShopRegStatus.ACCEPTED);
        shopRegRepository.save(shopReg);

        Shop shop = optionalShop.get();
        shop.setName(shopReg.getName());
        shop.setDescription(shopReg.getDescription());
        shop.setCategory(shopReg.getCategory());
        shop.setStatus(ShopStatus.OPEN);

        return ShopDto.fromEntity(shopRepository.save(shop));
    }

    public ShopRegResponseDto declineShopReg(Long regId, ShopRegDeclineDto dto) {
        UserEntity currentUser = facade.getCurrentUserEntity();
        // admin만 허가 가능
        if (!currentUser.getRole().equals(UserRole.ROLE_ADMIN))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Optional<ShopRegistration> optionalReg = shopRegRepository.findById(regId);
        // 등록 신청이 없는 경우
        if (optionalReg.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        ShopRegistration shopReg = optionalReg.get();
        // 이미 처리된 Shop 등록 요청인 경우
        if (!shopReg.getStatus().equals(ShopRegStatus.WAITING))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        shopReg.setStatus(ShopRegStatus.DECLINED);
        shopReg.setDeclineReason(dto.getReason());
        return ShopRegResponseDto.fromEntity(shopRegRepository.save(shopReg));
    }

    public ShopCloseResponseDto shopCloseRequest(ShopCloseRequestDto dto) {
        UserEntity currentUser = facade.getCurrentUserEntity();

        // 사업자 사용자만 폐쇄 신청 가능
        if (!currentUser.getRole().equals(UserRole.ROLE_BUSINESS))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Optional<Shop> optionalShop = shopRepository.findByOwnerId(currentUser.getId());
        if (optionalShop.isEmpty())
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        Shop shop = optionalShop.get();

        // 이미 신청한 요청인지 확인
        if (closeRequestRepository.existsByOwnerId(currentUser.getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        // 오픈중인 Shop만 Close 가능
        if (!shop.getStatus().equals(ShopStatus.OPEN))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        ShopCloseRequest closeRequest = ShopCloseRequest.builder()
                .owner(currentUser)
                .reason(dto.getReason())
                .build();
        return ShopCloseResponseDto.fromEntity(closeRequestRepository.save(closeRequest));
    }

    public List<ShopCloseResponseDto> readAllCloseRequest() {
        UserEntity currentUser = facade.getCurrentUserEntity();

        // admin만 조회 가능
        if (!currentUser.getRole().equals(UserRole.ROLE_ADMIN))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        return closeRequestRepository.findAll().stream()
                .map(ShopCloseResponseDto::fromEntity)
                .toList();
    }

    @Transactional
    public ShopDto closeShop(Long reqId) {
        UserEntity currentUser = facade.getCurrentUserEntity();

        // admin만 조회 가능
        if (!currentUser.getRole().equals(UserRole.ROLE_ADMIN))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Optional<ShopCloseRequest> optionalRequest = closeRequestRepository.findById(reqId);
        // request가 존재하지 않는 경우 BAD_REQUEST
        if (optionalRequest.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        ShopCloseRequest request = optionalRequest.get();
        UserEntity owner = request.getOwner();

        Optional<Shop> optionalShop = shopRepository.findByOwnerId(owner.getId());
        // owner의 shop이 없는 경우 에러
        if (optionalShop.isEmpty())
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);

        closeRequestRepository.delete(request);

        Shop shop = optionalShop.get();
        shop.setStatus(ShopStatus.CLOSED);
        return ShopDto.fromEntity(shopRepository.save(shop));
    }

    public List<ShopDto> searchShops(String nameQ, String category) {
        UserEntity currentUser = facade.getCurrentUserEntity();
        // 비활성사용자는 조회불가
        if (currentUser.getRole().equals(UserRole.ROLE_INACTIVE))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        List<Shop> shopList;
        if (nameQ == null && category == null) {
            // 검색 조건이 없는 경우
            shopList = shopRepository.findAllByOrderByLastPurchasedDesc();
        }
        if (nameQ != null && category == null) {
            // 이름만 가지고 검색
            shopList = shopRepository.findAllByNameContaining(nameQ);
        } else if (nameQ == null && category != null) {
            // 카테고리만으로 검색
            try {
                shopList = shopRepository.findAllByCategory(
                        ShopCategory.valueOf(category));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        } else {
            try {
                shopList = shopRepository.findAllByNameContainingAndCategory(
                        nameQ, ShopCategory.valueOf(category));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        }

        return shopList.stream().map(ShopDto::fromEntity).toList();
    }
}