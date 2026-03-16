package com.aghaz.orderbook.trading.infra.repo;

import com.aghaz.orderbook.trading.infra.entity.TradeFillEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TradeFillRepo extends JpaRepository<TradeFillEntity, Long> {

    List<TradeFillEntity> findTop100ByInstrumentOrderByExecutedAtDesc(String instrument);

    List<TradeFillEntity> findTop100ByBuyTraderIdOrSellTraderIdOrderByExecutedAtDesc(long buyTraderId, long sellTraderId);

    Page<TradeFillEntity> findByBuyTraderIdOrSellTraderId(long buyTraderId, long sellTraderId, Pageable pageable);

    Page<TradeFillEntity> findByInstrument(String instrument, Pageable pageable);

    @Query("""
            select t.instrument, count(t)
            from TradeFillEntity t
            group by t.instrument
            order by count(t) desc
            """)
    List<Object[]> countTradesByInstrument();
}