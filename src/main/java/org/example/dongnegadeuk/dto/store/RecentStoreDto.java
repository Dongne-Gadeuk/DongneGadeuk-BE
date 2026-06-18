package org.example.dongnegadeuk.dto.store;

import org.example.dongnegadeuk.entity.Category;

import java.time.LocalDateTime;

public record RecentStoreDto(
        Long storeId,
        String storeName,
        String address,
        Category category,
        String storeImageUrl,
        Integer visitCount,
        Double latitude,
        Double longitude,
        LocalDateTime lastVisitedAt
) {
}