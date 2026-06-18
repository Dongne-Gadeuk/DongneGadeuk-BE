package org.example.dongnegadeuk.controller;

import lombok.RequiredArgsConstructor;
import org.example.dongnegadeuk.dto.OwnedItemResponse;
import org.example.dongnegadeuk.dto.PlacementResponse;
import org.example.dongnegadeuk.dto.RoomSyncRequest;
import org.example.dongnegadeuk.dto.RoomSyncResponse;
import org.example.dongnegadeuk.service.RoomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static reactor.netty.http.HttpConnectionLiveness.log;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    // 내 보유 아이템 (바텀시트 / 내 컬렉션)
    @GetMapping("/items")
    public List<OwnedItemResponse> getItems() {
        return roomService.getOwnedItems(currentUserId());
    }

    // 내 방 배치 현황
    @GetMapping("/room")
    public List<PlacementResponse> getRoom() {
        return roomService.getRoom(currentUserId());
    }

    // 꾸미기 저장 (추가/수정/삭제 한 번에)
    @PostMapping("/room/sync")
    public RoomSyncResponse sync(@RequestBody RoomSyncRequest request) {
        return roomService.sync(currentUserId(), request);
    }

    // TODO: 인증 붙이면 @AuthenticationPrincipal 등에서 추출.
    // 지금은 임시 고정값. (Spring Security 연동 전 단계)
    private Long currentUserId() {
        return 1L;
    }
}