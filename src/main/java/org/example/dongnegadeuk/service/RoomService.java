package org.example.dongnegadeuk.service;

import lombok.RequiredArgsConstructor;
import org.example.dongnegadeuk.dto.*;
import org.example.dongnegadeuk.entity.Placements;
import org.example.dongnegadeuk.entity.UserItems;
import org.example.dongnegadeuk.repository.PlacementsRepository;
import org.example.dongnegadeuk.repository.UserItemsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final UserItemsRepository userItemsRepository;
    private final PlacementsRepository placementsRepository;

    // 보유 아이템 (바텀시트/컬렉션). placed 플래그 포함 → 프론트가 미배치만 필터.
    @Transactional(readOnly = true)
    public List<OwnedItemResponse> getOwnedItems(Long userId) {
        return userItemsRepository.findAllByUserId(userId).stream()
                .map(OwnedItemResponse::from)
                .toList();
    }

    // 방 배치 현황 (초기 로드)
    @Transactional(readOnly = true)
    public List<PlacementResponse> getRoom(Long userId) {
        return placementsRepository.findAllByUserId(userId).stream()
                .map(PlacementResponse::from)
                .toList();
    }

    /**
     * 저장 = diff 한 번에 처리.
     *  1) 삭제 먼저 (removedIds)  → placement 삭제 + UserItems.placed=false
     *  2) 수정 (updated)         → x/y/order/scale/반전 갱신
     *  3) 추가 (added)           → INSERT + UserItems.placed=true, tempId->새 id 매핑
     *  ※ order 의 1씩 정규화는 별도 일일 배치에서 수행 (여기선 받은 값 그대로 저장).
     */
    @Transactional
    public RoomSyncResponse sync(Long userId, RoomSyncRequest req) {

        // 1) 삭제
        if (req.removedIds() != null) {
            for (Long placementId : req.removedIds()) {
                placementsRepository.findById(placementId).ifPresent(p -> {
                    assertOwner(p.getUserItem(), userId);
                    p.getUserItem().setPlaced(false);
                    placementsRepository.delete(p);
                });
            }
        }

        // 2) 수정
        if (req.updated() != null) {
            for (PlacementInput in : req.updated()) {
                Placements p = placementsRepository.findById(in.placementId())
                        .orElseThrow(() -> notFound("placement", in.placementId()));
                assertOwner(p.getUserItem(), userId);
                apply(p, in);
            }
        }

        // 3) 추가 (+ idMap)
        Map<String, Long> idMap = new HashMap<>();
        if (req.added() != null) {
            for (PlacementInput in : req.added()) {
                UserItems ui = userItemsRepository.findById(in.userItemId())
                        .orElseThrow(() -> notFound("userItem", in.userItemId()));
                assertOwner(ui, userId);

                // user_item_id 에 유니크 제약이 있으므로,
                // 이미 배치된 아이템이면 새로 insert 하지 않고 기존 행을 갱신한다.
                Placements p = placementsRepository.findByUserItem_UserItemId(in.userItemId())
                        .orElseGet(() -> Placements.builder()
                                .userItem(ui)
                                .scale(BigDecimal.valueOf(1.0))   // 신규 생성 시 기본값
                                .build());

                apply(p, in);   // x/y/zOrder/scale/반전 일괄 적용

                Placements saved = placementsRepository.save(p);
                ui.setPlaced(true);

                if (in.tempId() != null) {
                    idMap.put(in.tempId(), saved.getPlacementId());
                }
            }
        }

        return new RoomSyncResponse(idMap);
    }

    private void apply(Placements p, PlacementInput in) {
        p.setX(in.x());
        p.setY(in.y());
        p.setZOrder(in.zOrder());
        if (in.scale() != null) p.setScale(in.scale());
        p.setTopBottom(Boolean.TRUE.equals(in.topBottom()));
        p.setLeftRight(Boolean.TRUE.equals(in.leftRight()));
    }

    private void assertOwner(UserItems ui, Long userId) {
        if (ui.getUser() == null || !ui.getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 아이템이 아닙니다.");
        }
    }

    private ResponseStatusException notFound(String what, Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, what + " not found: " + id);
    }
}