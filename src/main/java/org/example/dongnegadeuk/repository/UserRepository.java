package org.example.dongnegadeuk.repository;

import org.example.dongnegadeuk.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    boolean existsByUsername(String username);
    boolean existsByNickname(String nickname);
    Optional<Users> findByUsername(String username);
}
