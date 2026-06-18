package org.example.dongnegadeuk.dto.store;


import java.util.List;

// 지도에서 최근 가게 카드 5개 띄우기
public record RecentStoreResponse(
        Integer totalCount,
        List<RecentStoreDto> stores
) {
}