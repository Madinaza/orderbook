package com.aghaz.orderbook.marketdata.infra.repo;

import com.aghaz.orderbook.marketdata.infra.entity.TradeHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TradeHistoryRepo extends JpaRepository<TradeHistoryEntity, Long> {
    Optional<TradeHistoryEntity> findByEventId(String eventId);
    List<TradeHistoryEntity> findTop100ByInstrumentOrderByExecutedAtDesc(String instrument);
}