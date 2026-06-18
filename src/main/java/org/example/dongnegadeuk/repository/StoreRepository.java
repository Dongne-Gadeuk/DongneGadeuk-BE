package org.example.dongnegadeuk.repository;

import org.example.dongnegadeuk.entity.Stores;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Stores, Long> {
}