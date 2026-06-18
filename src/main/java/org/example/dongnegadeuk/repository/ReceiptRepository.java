package org.example.dongnegadeuk.repository;

import org.example.dongnegadeuk.entity.Receipts;
import org.example.dongnegadeuk.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ReceiptRepository extends JpaRepository<Receipts, Long> {

    boolean existsByUser_UserIdAndStore_StoreId(Long userId, Long storeId);

    boolean existsByUserAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(Users user, LocalDateTime start, LocalDateTime end);
}
