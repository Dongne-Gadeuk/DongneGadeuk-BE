package org.example.dongnegadeuk.controller;

import lombok.RequiredArgsConstructor;
import org.example.dongnegadeuk.dto.item.CollectionResponse;
import org.example.dongnegadeuk.service.CollectionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    // 아이템 리스트
    @GetMapping("/collection")
    public CollectionResponse getCollection(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "ALL") String category
    ) {
        return collectionService.getCollection(userId, category);
    }
}