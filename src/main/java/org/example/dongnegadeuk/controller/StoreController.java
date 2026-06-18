package org.example.dongnegadeuk.controller;

import lombok.RequiredArgsConstructor;
import org.example.dongnegadeuk.dto.store.RecentStoreResponse;
import org.example.dongnegadeuk.dto.store.StoreItemListResponse;
import org.example.dongnegadeuk.service.StoreService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    // 지도 - 최근 5곳
    @GetMapping("/recent")
    public RecentStoreResponse getRecentStores(
            @RequestParam Long userId
    ) {
        return storeService.getRecentStores(userId);
    }

    // 가게별 아이템 리스트
    @GetMapping("/{storeId}/items")
    public StoreItemListResponse getStoreItems(
            @PathVariable Long storeId,
            @RequestParam Long userId
    ) {
        return storeService.getStoreItems(userId, storeId);
    }
}