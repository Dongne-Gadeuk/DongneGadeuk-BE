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

    // updated/removed 대상 한 번에 로딩 + owner(user)까지 fetch → assertOwner 지연로딩 제거
    @Query("select p from Placements p " +
            "join fetch p.userItem ui " +
            "join fetch ui.user " +
            "where p.placementId in :ids")
    List<Placements> findAllByIdInFetchOwner(@Param("ids") List<Long> ids);

    // added 의 upsert 판별용 (userItemId 들로 기존 placement 일괄 조회)
    @Query("select p from Placements p " +
            "join fetch p.userItem ui " +
            "where ui.userItemId in :ids")
    List<Placements> findAllByUserItemIdIn(@Param("ids") List<Long> ids);
}