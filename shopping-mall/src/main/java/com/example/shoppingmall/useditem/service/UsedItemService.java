package com.example.shoppingmall.useditem.service;

import com.example.shoppingmall.AuthenticationFacade;
import com.example.shoppingmall.useditem.dto.UsedItemOrderDto;
import com.example.shoppingmall.useditem.dto.UsedItemDto;
import com.example.shoppingmall.useditem.entity.UsedItemStatus;
import com.example.shoppingmall.useditem.entity.UsedItemOrderStatus;
import com.example.shoppingmall.useditem.entity.UsedItemOrder;
import com.example.shoppingmall.useditem.repo.UsedItemOrderRepository;
import com.example.shoppingmall.useditem.repo.UsedItemRepository;
import com.example.shoppingmall.user.entity.UserRole;
import com.example.shoppingmall.useditem.entity.UsedItem;
import com.example.shoppingmall.user.entity.UserEntity;
import com.example.shoppingmall.user.repo.UserRepository;
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
public class UsedItemService {
    private final UsedItemRepository usedItemRepository;
    private final UserRepository userRepository;
    private final UsedItemOrderRepository usedItemOrderRepository;
    private final AuthenticationFacade facade;

    public UsedItemDto createUsedItem(UsedItemDto dto) {
        UserEntity currentUser = facade.getCurrentUserEntity();

        // 일반 사용자의 경우에만 아이템 등록 가능
        if (!currentUser.getRole().equals(UserRole.ROLE_USER))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        UsedItem usedItem = UsedItem.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .user(currentUser)
                .status(UsedItemStatus.ON_SALE)
                .build();
        return UsedItemDto.fromEntity(usedItemRepository.save(usedItem));
    }

    public List<UsedItemDto> readAllUsedItem() {
        UserEntity currentUser = facade.getCurrentUserEntity();

        if (currentUser.getRole().equals(UserRole.ROLE_INACTIVE))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        return usedItemRepository.findAll().stream()
                .map(UsedItemDto::fromEntity)
                .toList();
    }

    public UsedItemDto updateUsedItem(Long id, UsedItemDto dto) {
        Optional<UsedItem> optionalItem = usedItemRepository.findById(id);
        if (optionalItem.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        UsedItem usedItem = optionalItem.get();
        UserEntity currentUser = facade.getCurrentUserEntity();
        if (!usedItem.getUser().getId().equals(currentUser.getId()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        usedItem.setTitle(dto.getTitle());
        usedItem.setDescription(dto.getDescription());
        usedItem.setPrice(dto.getPrice());
        return UsedItemDto.fromEntity(usedItemRepository.save(usedItem));
    }

    public void deleteUsedItem(Long id) {
        Optional<UsedItem> optionalItem = usedItemRepository.findById(id);
        if (optionalItem.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        UsedItem usedItem = optionalItem.get();
        UserEntity currentUser = facade.getCurrentUserEntity();
        if (!usedItem.getUser().getUsername().equals(currentUser.getUsername()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        usedItemRepository.deleteById(id);
    }

    public UsedItemOrderDto createOrder(Long itemId) {
        Optional<UsedItem> optionalItem = usedItemRepository.findById(itemId);
        // item이 존재하지 않을 경우
        if (optionalItem.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        UsedItem usedItem = optionalItem.get();
        // 판매 완료일시 새로운 구매 불가
        if (usedItem.getStatus().equals(UsedItemStatus.SOLD_OUT))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        UserEntity currentUser = facade.getCurrentUserEntity();
        // 비활성회원은 구매 불가
        if (currentUser.getRole().equals(UserRole.ROLE_INACTIVE))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        // 등록자와 구매자가 같은 경우 구매 불가
        if (usedItem.getUser().getId().equals(currentUser.getId()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        UsedItemOrder order = UsedItemOrder.builder()
                .item(usedItem)
                .buyer(currentUser)
                .status(UsedItemOrderStatus.WAITING)
                .build();

        return UsedItemOrderDto.fromEntity(usedItemOrderRepository.save(order));
    }

    public List<UsedItemOrderDto> readItemOrders(Long itemId) {
        Optional<UsedItem> optionalItem = usedItemRepository.findById(itemId);
        // 아이템이 존재하지 않는 경우
        if (optionalItem.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        // 비활성회원은 조회 불가
        UserEntity currentUser = facade.getCurrentUserEntity();
        if (currentUser.getRole().equals(UserRole.ROLE_INACTIVE))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        UsedItem usedItem = optionalItem.get();

        // 조회자가 아이템의 소유자인 경우
        if (usedItem.getUser().getId().equals(currentUser.getId())) {
            return usedItemOrderRepository.findAllByItemId(itemId).stream()
                    .map(UsedItemOrderDto::fromEntity)
                    .toList();
        } else {
            return usedItemOrderRepository.findAllByItemIdAndBuyerId(itemId, currentUser.getId())
                    .stream()
                    .map(UsedItemOrderDto::fromEntity)
                    .toList();
        }
    }
}
