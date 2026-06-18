package org.example.dongnegadeuk.repository;

import org.example.dongnegadeuk.entity.UserItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserItemsRepository extends JpaRepository<UserItems, Long> {

    @Query("select ui from UserItems ui " +
            "join fetch ui.item " +
            "where ui.user.userId = :userId")
    List<UserItems> findAllByUserId(@Param("userId") Long userId);

    // added 의 userItemId 들 한 번에 + user fetch
    @Query("select ui from UserItems ui " +
            "join fetch ui.user " +
            "where ui.userItemId in :ids")
    List<UserItems> findAllByIdInFetchUser(@Param("ids") List<Long> ids);
}