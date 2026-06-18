package org.example.dongnegadeuk.dto;

import java.math.BigDecimal;

// 저장 diff 의 한 행 (added / updated 공용)
public record PlacementInput(
        Long placementId,   // updated: 기존 row id
        String tempId,      // added: 프론트 임시키 (응답 idMap 매핑용)
        Long userItemId,    // 어떤 보유 아이템인지
        Integer x,
        Integer y,
        Integer zOrder,      // z (1이 제일 안쪽)
        BigDecimal scale,
        Boolean topBottom,
        Boolean leftRight
) {}