package org.example.dongnegadeuk.controller;

import lombok.RequiredArgsConstructor;
import org.example.dongnegadeuk.common.exception.CustomException;
import org.example.dongnegadeuk.dto.room.OwnedItemResponse;
import org.example.dongnegadeuk.dto.room.PlacementResponse;
import org.example.dongnegadeuk.dto.room.RoomSyncRequest;
import org.example.dongnegadeuk.dto.room.RoomSyncResponse;
import org.example.dongnegadeuk.entity.Users;
import org.example.dongnegadeuk.service.RoomService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


import static org.example.dongnegadeuk.common.exception.errorCode.JwtErrorCode.JWT_MISSING;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    // 내 보유 아이템 (바텀시트 / 내 컬렉션)
    @GetMapping("/items")
    public List<OwnedItemResponse> getItems(@AuthenticationPrincipal Users user) {
        if (user == null) {
            throw new CustomException(JWT_MISSING);
        }
        return roomService.getOwnedItems(user.getUserId());
    }

    // 내 방 배치 현황
    @GetMapping("/room")
    public List<PlacementResponse> getRoom(@AuthenticationPrincipal Users user) {
        if (user == null) {
            throw new CustomException(JWT_MISSING);
        }
        return roomService.getRoom(user.getUserId());
    }

    // 꾸미기 저장 (추가/수정/삭제 한 번에)
    @PostMapping("/room/sync")
    public RoomSyncResponse sync(
            @AuthenticationPrincipal Users user,
            @RequestBody RoomSyncRequest request
    ) {
        if (user == null) {
            throw new CustomException(JWT_MISSING);
        }
        return roomService.sync(user.getUserId(), request);
    }
}