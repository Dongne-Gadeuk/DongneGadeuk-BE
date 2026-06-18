package org.example.dongnegadeuk.repository;

import org.example.dongnegadeuk.entity.Placements;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlacementsRepository extends JpaRepository<Placements, Long> {

    // 방 배치 + userItem + item 조인 (N+1 방지)
    @Query("select p from Placements p " +
            "join fetch p.userItem ui " +
            "join fetch ui.item " +
            "where ui.user.userId = :userId")
    List<Placements> findAllByUserId(@Param("userId") Long userId);
    Optional<Placements> findByUserItem_UserItemId(Long userItemId);

}