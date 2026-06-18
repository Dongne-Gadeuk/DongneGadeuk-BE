package org.example.dongnegadeuk.service;

import lombok.RequiredArgsConstructor;
import org.example.dongnegadeuk.dto.room.*;
import org.example.dongnegadeuk.entity.Placements;
import org.example.dongnegadeuk.entity.UserItems;
import org.example.dongnegadeuk.repository.PlacementsRepository;
import org.example.dongnegadeuk.repository.UserItemsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final UserItemsRepository userItemsRepository;
    private final PlacementsRepository placementsRepository;

    // 보유 아이템 (바텀시트/컬렉션)
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

    @Transactional
    public RoomSyncResponse sync(Long userId, RoomSyncRequest req) {

        // 1) 삭제 — 한 번에 로딩(owner fetch) → placed=false → 일괄 삭제 → flush
        if (req.removedIds() != null && !req.removedIds().isEmpty()) {
            List<Placements> toRemove = placementsRepository.findAllByIdInFetchOwner(req.removedIds());
            for (Placements p : toRemove) {
                assertOwner(p.getUserItem(), userId);
                p.getUserItem().setPlaced(false);
            }
            placementsRepository.deleteAllInBatch(toRemove);
            placementsRepository.flush();
        }

        // 2) 수정 — placementId 들 한 번에 로딩해 Map 처리
        if (req.updated() != null && !req.updated().isEmpty()) {
            List<Long> ids = req.updated().stream().map(PlacementInput::placementId).toList();
            Map<Long, Placements> found = placementsRepository.findAllByIdInFetchOwner(ids).stream()
                    .collect(Collectors.toMap(Placements::getPlacementId, p -> p));

            for (PlacementInput in : req.updated()) {
                Placements p = found.get(in.placementId());
                if (p == null) throw notFound("placement", in.placementId());
                assertOwner(p.getUserItem(), userId);
                apply(p, in);
            }
        }

        // 3) 추가 (+ idMap)
        Map<String, Long> idMap = new HashMap<>();
        if (req.added() != null && !req.added().isEmpty()) {
            List<Long> userItemIds = req.added().stream().map(PlacementInput::userItemId).toList();

            Map<Long, UserItems> uiMap = userItemsRepository.findAllByIdInFetchUser(userItemIds).stream()
                    .collect(Collectors.toMap(UserItems::getUserItemId, ui -> ui));

            Map<Long, Placements> existing = placementsRepository.findAllByUserItemIdIn(userItemIds).stream()
                    .collect(Collectors.toMap(p -> p.getUserItem().getUserItemId(), p -> p));

            List<PlacementInput> added = req.added();
            List<Placements> entities = new ArrayList<>(added.size());

            for (PlacementInput in : added) {
                UserItems ui = uiMap.get(in.userItemId());
                if (ui == null) throw notFound("userItem", in.userItemId());
                assertOwner(ui, userId);

                Placements p = existing.getOrDefault(in.userItemId(),
                        Placements.builder()
                                .userItem(ui)
                                .scale(BigDecimal.valueOf(1.0))
                                .build());

                apply(p, in);
                ui.setPlaced(true);
                entities.add(p);
            }

            placementsRepository.saveAll(entities);

            for (int i = 0; i < added.size(); i++) {
                String tempId = added.get(i).tempId();
                if (tempId != null) idMap.put(tempId, entities.get(i).getPlacementId());
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