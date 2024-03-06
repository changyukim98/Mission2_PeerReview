package com.example.shoppingmall.useditem.repo;

import com.example.shoppingmall.useditem.entity.UsedItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsedItemRepository
    extends JpaRepository<UsedItem, Long> {
}
