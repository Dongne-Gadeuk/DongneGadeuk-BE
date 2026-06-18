package org.example.dongnegadeuk.dto;

import org.example.dongnegadeuk.entity.Placements;

import java.math.BigDecimal;

// GET /api/me/room — 방에 배치된 아이템 (+ 표시용 imageUrl)
public record PlacementResponse(
        Long placementId,
        Long userItemId,
        String imageUrl,
        int x,
        int y,
        int zOrder,
        BigDecimal scale,
        boolean topBottom,
        boolean leftRight
) {
    public static PlacementResponse from(Placements p) {
        return new PlacementResponse(
                p.getPlacementId(),
                p.getUserItem().getUserItemId(),
                p.getUserItem().getItem().getImageUrl(),
                p.getX(),
                p.getY(),
                p.getZOrder(),
                p.getScale(),
                Boolean.TRUE.equals(p.getTopBottom()),
                Boolean.TRUE.equals(p.getLeftRight())
        );
    }
}