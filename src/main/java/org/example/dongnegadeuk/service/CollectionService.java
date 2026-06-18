package org.example.dongnegadeuk.service;

import lombok.RequiredArgsConstructor;
import org.example.dongnegadeuk.dto.item.CollectionItemDto;
import org.example.dongnegadeuk.dto.item.CollectionResponse;
import org.example.dongnegadeuk.entity.Category;
import org.example.dongnegadeuk.entity.UserItems;
import org.example.dongnegadeuk.repository.UserItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionService {

    private final UserItemRepository userItemRepository;

    // 아이템 도감
    public CollectionResponse getCollection(Long userId, String category) {
        String selectedCategory = category == null ? "ALL" : category.toUpperCase();

        List<UserItems> userItems;

        // 전체, 카테고리별
        if (selectedCategory.equals("ALL")) {
            userItems = userItemRepository.findByUser_UserIdOrderByRequiredAtAsc(userId);
        } else {
            Category parsedCategory = Category.valueOf(selectedCategory);

            userItems = userItemRepository
                    .findByUser_UserIdAndItem_Store_CategoryOrderByRequiredAtAsc(
                            userId,
                            parsedCategory
                    );
        }

        List<CollectionItemDto> items = userItems.stream()
                .map(userItem -> new CollectionItemDto(
                        userItem.getUserItemId(),
                        userItem.getItem().getItemId(),
                        userItem.getItem().getItemName(),
                        userItem.getItem().getStore().getCategory(),
                        userItem.getItem().getImageUrl(),
                        userItem.getRequiredAt(),
                        userItem.getPlaced()
                ))
                .toList();

        // 마지막 잠금 카드는 프론트에서 따로 lock.svg 설정해줘야됨
        return new CollectionResponse(
                selectedCategory,
                items
        );
    }
}