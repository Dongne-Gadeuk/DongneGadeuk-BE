package org.example.dongnegadeuk.repository;

import org.example.dongnegadeuk.entity.UserStoreVisits;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserStoreVisitRepository extends JpaRepository<UserStoreVisits, Long> {

    // 유저가 해당 가게 몇번 방문했는지
    Optional<UserStoreVisits> findByUser_UserIdAndStore_StoreId(Long userId, Long storeId);
}