package org.example.dongnegadeuk.dto;

import java.util.List;

// 프론트 diff 그대로: added / updated / removedIds
public record RoomSyncRequest(
        List<PlacementInput> added,
        List<PlacementInput> updated,
        List<Long> removedIds
) {}