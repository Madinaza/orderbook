package com.aghaz.orderbook.trading.app;

import com.aghaz.orderbook.trading.domain.OrderType;
import com.aghaz.orderbook.trading.domain.Side;
import com.aghaz.orderbook.trading.dto.PlaceOrderRequest;
import com.aghaz.orderbook.trading.dto.ReplaceOrderRequest;
import com.aghaz.orderbook.trading.infra.entity.LimitOrderEntity;
import com.aghaz.orderbook.trading.infra.entity.OrderAuditLogEntity;
import com.aghaz.orderbook.trading.infra.entity.TradeFillEntity;
import com.aghaz.orderbook.trading.infra.repo.LimitOrderRepo;
import com.aghaz.orderbook.trading.infra.repo.OrderAuditLogRepo;
import com.aghaz.orderbook.trading.infra.repo.TradeFillRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderCommandServiceIntegrationTest {

    @Autowired
    OrderCommandService orderCommandService;

    @Autowired
    LimitOrderRepo limitOrderRepo;

    @Autowired
    TradeFillRepo tradeFillRepo;

    @Autowired
    OrderAuditLogRepo orderAuditLogRepo;

    @BeforeEach
    void clean() {
        orderAuditLogRepo.deleteAll();
        tradeFillRepo.deleteAll();
        limitOrderRepo.deleteAll();
    }

    @Test
    void place_shouldPersistTradeAndAudit_whenMatchingRestingOrder() {
        LimitOrderEntity restingSell = limitOrderRepo.save(
                LimitOrderEntity.place(
                        2L,
                        "AAPL",
                        Side.SELL,
                        OrderType.LIMIT,
                        new BigDecimal("100.00"),
                        5
                )
        );

        var response = orderCommandService.place(
                1L,
                new PlaceOrderRequest(
                        "AAPL",
                        Side.BUY,
                        OrderType.LIMIT,
                        new BigDecimal("101.00"),
                        3
                )
        );

        assertNotNull(response.id());
        assertEquals("FILLED", response.status());

        LimitOrderEntity updatedSell = limitOrderRepo.findById(restingSell.getId()).orElseThrow();
        assertEquals(2, updatedSell.getOpenQty());

        List<TradeFillEntity> trades = tradeFillRepo.findTop100ByInstrumentOrderByExecutedAtDesc("AAPL");
        assertEquals(1, trades.size());
        assertEquals(3, trades.get(0).getQuantity());
        assertEquals(0, trades.get(0).getPrice().compareTo(new BigDecimal("100.00")));

        List<OrderAuditLogEntity> buyerEvents =
                orderAuditLogRepo.findAllByOrderIdOrderByCreatedAtAsc(response.id());

        assertTrue(buyerEvents.stream().anyMatch(e -> e.getEventType().equals("PLACED")));
        assertTrue(buyerEvents.stream().anyMatch(e -> e.getEventType().equals("FILLED")));
    }

    @Test
    void replace_shouldWorkAfterPartialFill_whenNewQtyExceedsFilledQty() {
        limitOrderRepo.save(
                LimitOrderEntity.place(
                        2L,
                        "AAPL",
                        Side.SELL,
                        OrderType.LIMIT,
                        new BigDecimal("100.00"),
                        4
                )
        );

        var response = orderCommandService.place(
                1L,
                new PlaceOrderRequest(
                        "AAPL",
                        Side.BUY,
                        OrderType.LIMIT,
                        new BigDecimal("100.00"),
                        10
                )
        );

        assertEquals("PARTIALLY_FILLED", response.status());
        assertEquals(6, response.openQty());

        var replaced = orderCommandService.replace(
                1L,
                response.id(),
                new ReplaceOrderRequest(new BigDecimal("101.00"), 12)
        );

        assertEquals("PARTIALLY_FILLED", replaced.status());
        assertEquals(12, replaced.originalQty());
        assertEquals(8, replaced.openQty());
        assertEquals(0, replaced.limitPrice().compareTo(new BigDecimal("101.00")));

        List<OrderAuditLogEntity> events =
                orderAuditLogRepo.findAllByOrderIdOrderByCreatedAtAsc(response.id());

        assertTrue(events.stream().anyMatch(e -> e.getEventType().equals("REPLACED")));
    }
}