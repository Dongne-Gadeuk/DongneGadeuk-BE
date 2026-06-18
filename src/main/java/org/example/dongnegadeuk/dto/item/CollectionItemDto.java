package org.example.dongnegadeuk.dto.item;

import org.example.dongnegadeuk.entity.Category;

import java.time.LocalDateTime;

// 아이템 도감의 카드 하나
public record CollectionItemDto(
        Long userItemId,
        Long itemId,
        String itemName,
        Category category,
        String imageUrl,
        LocalDateTime requiredAt,
        Boolean placed
) {
}