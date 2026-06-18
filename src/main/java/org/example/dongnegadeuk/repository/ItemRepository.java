package org.example.dongnegadeuk.repository;

import org.example.dongnegadeuk.entity.Items;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Items, Long> {

    Optional<Items> findByStore_StoreId(Long storeId);

    Optional<Items> findByItemName(String itemName);

    // 특정 가게에서 얻을 수 있는 아이템 리스트 조회
    List<Items> findByStore_StoreIdOrderByRequiredVisitCountAsc(Long storeId);
}