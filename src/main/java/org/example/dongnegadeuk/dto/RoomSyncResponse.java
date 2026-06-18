package org.example.dongnegadeuk.dto;

import java.util.Map;

// 신규 INSERT 된 행의 tempId -> 서버 발급 placementId 매핑
public record RoomSyncResponse(
        Map<String, Long> idMap
) {}