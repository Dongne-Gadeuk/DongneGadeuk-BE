package org.example.dongnegadeuk.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class ItemReceiveDto {
    private ScanResultType type;
    private StoreInfo store;
    private RewardItem item;

    public enum ScanResultType { ITEM_CREATE, VISIT_ONLY }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StoreInfo {
        private Long storeId;
        private String storeName;
        private LocalDate transactionDate;
        private int visitCount;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class RewardItem {
        private Long itemId;
        private String name;
        private String imageUrl;
    }
}