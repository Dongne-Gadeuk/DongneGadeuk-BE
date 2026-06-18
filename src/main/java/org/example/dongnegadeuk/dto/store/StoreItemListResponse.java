package org.example.dongnegadeuk.dto.store;

import org.example.dongnegadeuk.entity.Category;

import java.util.List;

public record StoreItemListResponse(
        Long storeId,
        String storeName,
        Category category,
        Integer visitCount,
        List<StoreItemDto> items
) {
}