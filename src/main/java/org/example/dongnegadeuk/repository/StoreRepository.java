package org.example.dongnegadeuk.repository;

import org.example.dongnegadeuk.entity.Stores;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Stores, Long> {

    Optional<Stores> findByBusinessNumberAndAddressAndStoreName(String businessNumber, String address, String storeName);
}