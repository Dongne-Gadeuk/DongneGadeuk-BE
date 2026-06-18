package org.example.dongnegadeuk.service;

import lombok.RequiredArgsConstructor;
import org.example.dongnegadeuk.dto.store.RecentStoreDto;
import org.example.dongnegadeuk.dto.store.RecentStoreResponse;
import org.example.dongnegadeuk.dto.store.StoreItemDto;
import org.example.dongnegadeuk.dto.store.StoreItemListResponse;
import org.example.dongnegadeuk.entity.Items;
import org.example.dongnegadeuk.entity.Stores;
import org.example.dongnegadeuk.entity.UserItems;
import org.example.dongnegadeuk.entity.UserStoreVisits;
import org.example.dongnegadeuk.repository.ItemRepository;
import org.example.dongnegadeuk.repository.StoreRepository;
import org.example.dongnegadeuk.repository.UserItemRepository;
import org.example.dongnegadeuk.repository.UserStoreVisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserStoreVisitRepository userStoreVisitRepository;
    private final ItemRepository itemRepository;
    private final UserItemRepository userItemRepository;

    // 첫 번째 화면: 지도에서 최근 방문한 가게 최대 5개 조회
    // 기준: UserItems.requiredAt, 즉 아이템 획득 시간
    public RecentStoreResponse getRecentStores(Long userId) {
        List<UserItems> recentUserItems =
                userItemRepository.findByUser_UserIdOrderByRequiredAtDesc(userId);

        Map<Long, Stores> recentStoreMap = new LinkedHashMap<>();

        for (UserItems userItem : recentUserItems) {
            Stores store = userItem.getItem().getStore();

            recentStoreMap.putIfAbsent(store.getStoreId(), store);

            if (recentStoreMap.size() == 5) {
                break;
            }
        }

        List<RecentStoreDto> stores = new ArrayList<>();

        for (Stores store : recentStoreMap.values()) {
            Integer visitCount = userStoreVisitRepository
                    .findByUser_UserIdAndStore_StoreId(userId, store.getStoreId())
                    .map(UserStoreVisits::getVisitCount)
                    .orElse(0);

            stores.add(new RecentStoreDto(
                    store.getStoreId(),
                    store.getStoreName(),
                    store.getAddress(),
                    store.getCategory(),
                    visitCount
            ));
        }

        return new RecentStoreResponse(
                stores.size(),
                stores
        );
    }

    // 두 번째 화면: 가게 클릭 시 해당 가게의 아이템 리스트 조회
    public StoreItemListResponse getStoreItems(Long userId, Long storeId) {
        Stores store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다."));

        Integer visitCount = userStoreVisitRepository
                .findByUser_UserIdAndStore_StoreId(userId, storeId)
                .map(UserStoreVisits::getVisitCount)
                .orElse(0);

        List<Items> storeItems =
                itemRepository.findByStore_StoreIdOrderByRequiredVisitCountAsc(storeId);

        List<UserItems> ownedUserItems =
                userItemRepository.findByUser_UserIdAndItem_Store_StoreId(userId, storeId);

        Map<Long, UserItems> ownedItemMap = ownedUserItems.stream()
                .collect(Collectors.toMap(
                        userItem -> userItem.getItem().getItemId(),
                        userItem -> userItem
                ));

        List<StoreItemDto> itemDtos = storeItems.stream()
                .map(item -> {
                    UserItems ownedUserItem = ownedItemMap.get(item.getItemId());
                    boolean owned = ownedUserItem != null;

                    return new StoreItemDto(
                            item.getItemId(),
                            owned ? ownedUserItem.getUserItemId() : null,
                            owned ? item.getItemName() : "아직 해금 X",
                            owned ? item.getImageUrl() : null,
                            item.getRequiredVisitCount(),
                            owned
                    );
                })
                .toList();

        return new StoreItemListResponse(
                store.getStoreId(),
                store.getStoreName(),
                visitCount,
                itemDtos
        );
    }
}