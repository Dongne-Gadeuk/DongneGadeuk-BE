package org.example.dongnegadeuk.dto.store;

import org.example.dongnegadeuk.entity.Category;

// 지도에서 가게 클릭했을 때 가게별 아이템 카드 1개
public record StoreItemDto(
        Long itemId,
        Long userItemId,
        String itemName,
        String imageUrl,
        Integer requiredVisitCount,

        // true면 실제 아이템 이미지, false면 lock.svg
        Boolean owned
) {
}