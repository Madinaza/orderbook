package com.aghaz.orderbook.marketdata.infra.repo;

import com.aghaz.orderbook.marketdata.infra.entity.OrderBookRowEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OrderBookRowRepo extends JpaRepository<OrderBookRowEntity, Long> {

    List<OrderBookRowEntity> findTop50ByInstrumentAndSideAndStatusInOrderByLimitPriceDescCreatedAtAsc(
            String instrument, String side, Collection<String> statuses
    );

    List<OrderBookRowEntity> findTop50ByInstrumentAndSideAndStatusInOrderByLimitPriceAscCreatedAtAsc(
            String instrument, String side, Collection<String> statuses
    );
}