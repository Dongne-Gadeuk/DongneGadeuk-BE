package org.example.dongnegadeuk.dto.store;

import org.example.dongnegadeuk.dto.RecentStoreDto;

import java.util.List;

public record RecentStoreResponse(
        List<RecentStoreDto> stores,
        int page,
        int size,
        boolean hasNext
) {
}