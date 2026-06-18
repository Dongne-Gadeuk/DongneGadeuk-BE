package org.example.dongnegadeuk.repository;

import org.example.dongnegadeuk.entity.Category;
import org.example.dongnegadeuk.entity.UserItems;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserItemRepository extends JpaRepository<UserItems, Long> {

    // 특정 유저가 특정 가게에서 획득한 아이템 조회
    List<UserItems> findByUser_UserIdAndItem_Store_StoreId(Long userId, Long storeId);

    // 도감 전체 조회
    List<UserItems> findByUser_UserIdOrderByRequiredAtAsc(Long userId);

    // 도감 카테고리별 조회
    List<UserItems> findByUser_UserIdAndItem_Store_CategoryOrderByRequiredAtAsc(
            Long userId,
            Category category
    );

    // 특정 아이템 보유 여부
    Optional<UserItems> findByUser_UserIdAndItem_ItemId(Long userId, Long itemId);

    // 지도 화면: 최근 아이템 획득순 조회
    List<UserItems> findByUser_UserIdOrderByRequiredAtDesc(Long userId);
}