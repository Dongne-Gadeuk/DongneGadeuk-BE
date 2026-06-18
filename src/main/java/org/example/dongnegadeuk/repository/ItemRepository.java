package org.example.dongnegadeuk.repository;

import org.example.dongnegadeuk.entity.Items;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Items, Long> {

    // 특정 가게에서 얻을 수 있는 아이템 리스트 조회
    List<Items> findByStore_StoreIdOrderByRequiredVisitCountAsc(Long storeId);
}