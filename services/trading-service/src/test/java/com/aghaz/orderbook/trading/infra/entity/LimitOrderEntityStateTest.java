package com.aghaz.orderbook.trading.infra.entity;

import com.aghaz.orderbook.trading.domain.OrderStatus;
import com.aghaz.orderbook.trading.domain.OrderType;
import com.aghaz.orderbook.trading.domain.Side;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class LimitOrderEntityStateTest {

    @Test
    void partialFillThenCancel_shouldMoveToCancelledAndZeroOpenQty() {
        LimitOrderEntity order = LimitOrderEntity.place(
                1L,
                "AAPL",
                Side.BUY,
                OrderType.LIMIT,
                new BigDecimal("100.00"),
                10
        );

        order.applyFill(4);

        assertEquals(OrderStatus.PARTIALLY_FILLED, order.getStatus());
        assertEquals(6, order.getOpenQty());

        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(0, order.getOpenQty());
        assertEquals(10, order.getOriginalQty());
    }

    @Test
    void replaceAfterPartialFill_shouldPreserveFilledQtyAndRecalculateOpenQty() {
        LimitOrderEntity order = LimitOrderEntity.place(
                1L,
                "AAPL",
                Side.BUY,
                OrderType.LIMIT,
                new BigDecimal("100.00"),
                10
        );

        order.applyFill(4);

        order.replace(new BigDecimal("101.00"), 12);

        assertEquals(new BigDecimal("101.00"), order.getLimitPrice());
        assertEquals(12, order.getOriginalQty());
        assertEquals(8, order.getOpenQty());
        assertEquals(OrderStatus.PARTIALLY_FILLED, order.getStatus());
        assertEquals(4, order.filledQty());
    }

    @Test
    void replaceAfterPartialFill_shouldRejectTotalQtyBelowAlreadyFilledQty() {
        LimitOrderEntity order = LimitOrderEntity.place(
                1L,
                "AAPL",
                Side.BUY,
                OrderType.LIMIT,
                new BigDecimal("100.00"),
                10
        );

        order.applyFill(4);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> order.replace(new BigDecimal("101.00"), 3)
        );

        assertTrue(ex.getMessage().contains("already filled"));
    }
}