package org.example.dongnegadeuk.repository;

import org.example.dongnegadeuk.entity.UserItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserItemsRepository extends JpaRepository<UserItems, Long> {

    // 보유 아이템 + Items 조인 (imageUrl/itemName 함께 로드)
    @Query("select ui from UserItems ui " +
            "join fetch ui.item " +
            "where ui.user.userId = :userId")
    List<UserItems> findAllByUserId(@Param("userId") Long userId);
}