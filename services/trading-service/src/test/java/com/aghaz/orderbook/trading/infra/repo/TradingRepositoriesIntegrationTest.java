package com.aghaz.orderbook.trading.infra.repo;

import com.aghaz.orderbook.trading.domain.OrderType;
import com.aghaz.orderbook.trading.domain.Side;
import com.aghaz.orderbook.trading.infra.entity.LimitOrderEntity;
import com.aghaz.orderbook.trading.infra.entity.OrderAuditLogEntity;
import com.aghaz.orderbook.trading.infra.entity.TradeFillEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TradingRepositoriesIntegrationTest {

    @Autowired
    LimitOrderRepo limitOrderRepo;

    @Autowired
    OrderAuditLogRepo orderAuditLogRepo;

    @Autowired
    TradeFillRepo tradeFillRepo;

    @BeforeEach
    void clean() {
        orderAuditLogRepo.deleteAll();
        tradeFillRepo.deleteAll();
        limitOrderRepo.deleteAll();
    }

    @Test
    void limitOrderRepo_shouldFindOwnedOrder() {
        LimitOrderEntity order = LimitOrderEntity.place(
                10L, "AAPL", Side.BUY, OrderType.LIMIT, new BigDecimal("100.00"), 10
        );

        LimitOrderEntity saved = limitOrderRepo.save(order);

        Optional<LimitOrderEntity> found = limitOrderRepo.findByIdAndTraderId(saved.getId(), 10L);
        Optional<LimitOrderEntity> notFound = limitOrderRepo.findByIdAndTraderId(saved.getId(), 99L);

        assertTrue(found.isPresent());
        assertTrue(notFound.isEmpty());
    }

    @Test
    void orderAuditLogRepo_shouldReturnEventsAscendingByCreatedAt() {
        LimitOrderEntity order = limitOrderRepo.save(
                LimitOrderEntity.place(1L, "AAPL", Side.BUY, OrderType.LIMIT, new BigDecimal("100.00"), 10)
        );

        orderAuditLogRepo.save(new OrderAuditLogEntity(
                order.getId(), 1L, "PLACED", "first", Instant.parse("2026-03-13T10:00:00Z")
        ));
        orderAuditLogRepo.save(new OrderAuditLogEntity(
                order.getId(), 1L, "FILLED", "second", Instant.parse("2026-03-13T10:01:00Z")
        ));

        List<OrderAuditLogEntity> events = orderAuditLogRepo.findAllByOrderIdOrderByCreatedAtAsc(order.getId());

        assertEquals(2, events.size());
        assertEquals("PLACED", events.get(0).getEventType());
        assertEquals("FILLED", events.get(1).getEventType());
    }

    @Test
    void tradeFillRepo_shouldReturnTradesByInstrumentAndTrader() {
        tradeFillRepo.save(new TradeFillEntity(
                "AAPL",
                10L,
                20L,
                1L,
                2L,
                new BigDecimal("100.00"),
                5L,
                Instant.parse("2026-03-13T10:00:00Z")
        ));

        tradeFillRepo.save(new TradeFillEntity(
                "MSFT",
                11L,
                21L,
                3L,
                4L,
                new BigDecimal("200.00"),
                2L,
                Instant.parse("2026-03-13T10:01:00Z")
        ));

        List<TradeFillEntity> aaplTrades = tradeFillRepo.findTop100ByInstrumentOrderByExecutedAtDesc("AAPL");
        List<TradeFillEntity> traderOneTrades = tradeFillRepo.findTop100ByBuyTraderIdOrSellTraderIdOrderByExecutedAtDesc(1L, 1L);

        assertEquals(1, aaplTrades.size());
        assertEquals("AAPL", aaplTrades.get(0).getInstrument());

        assertEquals(1, traderOneTrades.size());
        assertEquals(1L, traderOneTrades.get(0).getBuyTraderId());
    }
}