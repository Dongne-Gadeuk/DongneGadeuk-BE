package org.example.dongnegadeuk.dto.item;

import java.util.List;

// 아이템 도감 카드 3열 리스트
public record CollectionResponse(
        String category,
        List<CollectionItemDto> items
) {
}