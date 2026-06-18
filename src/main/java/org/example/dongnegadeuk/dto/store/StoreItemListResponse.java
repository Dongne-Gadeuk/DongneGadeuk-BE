package org.example.dongnegadeuk.dto.store;

import org.example.dongnegadeuk.entity.Category;

import java.util.List;

// 가게별 아이템 카드 리스트로 나열
public record StoreItemListResponse(
        Long storeId,
        String storeName,
        Integer visitCount,
        List<StoreItemDto> items
) {
}