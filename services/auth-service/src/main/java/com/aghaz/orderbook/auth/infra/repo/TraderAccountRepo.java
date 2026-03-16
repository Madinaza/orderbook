package com.aghaz.orderbook.auth.infra.repo;

import com.aghaz.orderbook.auth.infra.entity.TraderAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TraderAccountRepo extends JpaRepository<TraderAccountEntity, Long> {
    boolean existsByUsername(String username);
    Optional<TraderAccountEntity> findByUsername(String username);
}