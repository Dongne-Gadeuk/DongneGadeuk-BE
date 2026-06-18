package org.example.dongnegadeuk.dto.store;

import org.example.dongnegadeuk.entity.Category;

import java.time.LocalDateTime;

// 지도에 뜨는 가게 카드 하나
public record RecentStoreDto(
        Long storeId,
        String storeName,
        String address,
        Category category,
        String storeUrl,
        Integer visitCount
) {
}