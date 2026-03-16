package com.aghaz.orderbook.trading.infra.repo;

import com.aghaz.orderbook.trading.domain.OrderStatus;
import com.aghaz.orderbook.trading.infra.entity.LimitOrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LimitOrderRepo extends JpaRepository<LimitOrderEntity, Long> {

    Optional<LimitOrderEntity> findByIdAndTraderId(long id, long traderId);

    List<LimitOrderEntity> findAllByTraderIdOrderByCreatedAtDesc(long traderId);

    Page<LimitOrderEntity> findAllByTraderId(long traderId, Pageable pageable);

    Optional<LimitOrderEntity> findByClientOrderId(String clientOrderId);

    List<LimitOrderEntity> findAllByInstrumentAndStatusIn(String instrument, Collection<OrderStatus> statuses);

    long countByStatus(OrderStatus status);

    @Query("select count(distinct o.instrument) from LimitOrderEntity o")
    long countDistinctInstruments();

    @Query("""
            select o.instrument, count(o)
            from LimitOrderEntity o
            group by o.instrument
            order by count(o) desc
            """)
    List<Object[]> countOrdersByInstrument();

    @Query("""
            select o.instrument, coalesce(sum(o.openQty), 0)
            from LimitOrderEntity o
            where o.status in ('NEW', 'PARTIALLY_FILLED')
            group by o.instrument
            order by coalesce(sum(o.openQty), 0) desc
            """)
    List<Object[]> sumOpenQuantityByInstrument();

    @Query("""
            select count(o)
            from LimitOrderEntity o
            where o.side = 'BUY'
            """)
    long countBuyOrders();

    @Query("""
            select count(o)
            from LimitOrderEntity o
            where o.side = 'SELL'
            """)
    long countSellOrders();
}