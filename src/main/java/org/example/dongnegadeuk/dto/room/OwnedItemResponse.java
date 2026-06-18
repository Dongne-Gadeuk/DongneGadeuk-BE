package org.example.dongnegadeuk.dto.room;

import org.example.dongnegadeuk.entity.UserItems;

public record OwnedItemResponse(
        Long userItemId,
        Long itemId,
        String itemName,
        String imageUrl,
        boolean placed,
        String storeName,   // 추가
        int visitCount      // 추가
) {
    public static OwnedItemResponse from(UserItems ui) {
        var item = ui.getItem();
        return new OwnedItemResponse(
                ui.getUserItemId(),
                item.getItemId(),
                item.getItemName(),
                item.getImageUrl(),
                Boolean.TRUE.equals(ui.getPlaced()),
                item.getStore().getStoreName(),   // ← Stores의 가게명 getter
                item.getRequiredVisitCount()
        );
    }
}