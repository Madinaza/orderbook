package com.aghaz.orderbook.trading.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchingEngineEdgeCaseTest {

    private final MatchingEngine engine = new MatchingEngine();

    @Test
    void exactPriceCross_shouldMatchWhenBuyEqualsBestAsk() {
        OrderBook book = new OrderBook("AAPL");

        BookOrder restingSell = limitOrder(
                1L, 200L, "AAPL", Side.SELL, "100.00", 5, Instant.parse("2026-03-13T10:00:00Z")
        );
        book.addResting(restingSell);

        BookOrder incomingBuy = limitOrder(
                2L, 100L, "AAPL", Side.BUY, "100.00", 5, Instant.parse("2026-03-13T10:01:00Z")
        );

        List<TradeFill> fills = engine.match(book, incomingBuy);

        assertEquals(1, fills.size());
        assertEquals(5, fills.get(0).quantity());
        assertEquals(0, fills.get(0).price().compareTo(new BigDecimal("100.00")));
        assertEquals(OrderStatus.FILLED, incomingBuy.status());
        assertEquals(OrderStatus.FILLED, restingSell.status());
    }

    @Test
    void noCross_shouldNotMatchWhenBuyPriceBelowBestAsk() {
        OrderBook book = new OrderBook("AAPL");

        BookOrder restingSell = limitOrder(
                1L, 200L, "AAPL", Side.SELL, "101.00", 5, Instant.parse("2026-03-13T10:00:00Z")
        );
        book.addResting(restingSell);

        BookOrder incomingBuy = limitOrder(
                2L, 100L, "AAPL", Side.BUY, "100.00", 5, Instant.parse("2026-03-13T10:01:00Z")
        );

        List<TradeFill> fills = engine.match(book, incomingBuy);

        assertTrue(fills.isEmpty());
        assertEquals(OrderStatus.NEW, incomingBuy.status());
        assertEquals(5, incomingBuy.openQty());

        BookOrder bestBid = book.peekBest(Side.BUY).orElseThrow();
        BookOrder bestAsk = book.peekBest(Side.SELL).orElseThrow();

        assertEquals(2L, bestBid.id());
        assertEquals(1L, bestAsk.id());
    }

    @Test
    void multipleFillsAcrossLevels_shouldConsumeSeveralRestingOrders() {
        OrderBook book = new OrderBook("AAPL");

        book.addResting(limitOrder(
                1L, 201L, "AAPL", Side.SELL, "100.00", 2, Instant.parse("2026-03-13T10:00:00Z")
        ));
        book.addResting(limitOrder(
                2L, 202L, "AAPL", Side.SELL, "101.00", 3, Instant.parse("2026-03-13T10:00:10Z")
        ));

        BookOrder incomingBuy = limitOrder(
                3L, 100L, "AAPL", Side.BUY, "101.00", 5, Instant.parse("2026-03-13T10:01:00Z")
        );

        List<TradeFill> fills = engine.match(book, incomingBuy);

        assertEquals(2, fills.size());

        assertEquals(2, fills.get(0).quantity());
        assertEquals(0, fills.get(0).price().compareTo(new BigDecimal("100.00")));

        assertEquals(3, fills.get(1).quantity());
        assertEquals(0, fills.get(1).price().compareTo(new BigDecimal("101.00")));

        assertEquals(OrderStatus.FILLED, incomingBuy.status());
        assertTrue(book.peekBest(Side.SELL).isEmpty());
    }

    @Test
    void fifoAtSamePrice_shouldMatchOlderOrderFirst() {
        OrderBook book = new OrderBook("AAPL");

        book.addResting(limitOrder(
                10L, 210L, "AAPL", Side.SELL, "100.00", 2, Instant.parse("2026-03-13T10:00:00Z")
        ));
        book.addResting(limitOrder(
                11L, 211L, "AAPL", Side.SELL, "100.00", 3, Instant.parse("2026-03-13T10:00:05Z")
        ));

        BookOrder incomingBuy = limitOrder(
                20L, 100L, "AAPL", Side.BUY, "100.00", 4, Instant.parse("2026-03-13T10:01:00Z")
        );

        List<TradeFill> fills = engine.match(book, incomingBuy);

        assertEquals(2, fills.size());

        assertEquals(10L, fills.get(0).sellOrderId());
        assertEquals(2, fills.get(0).quantity());

        assertEquals(11L, fills.get(1).sellOrderId());
        assertEquals(2, fills.get(1).quantity());

        assertEquals(OrderStatus.FILLED, incomingBuy.status());

        BookOrder bestRemainingAsk = book.peekBest(Side.SELL).orElseThrow();
        assertEquals(11L, bestRemainingAsk.id());
        assertEquals(1, bestRemainingAsk.openQty());
    }

    @Test
    void marketOrderWithInsufficientLiquidity_shouldPartiallyFillAndLeaveRemainingOpen() {
        OrderBook book = new OrderBook("AAPL");

        book.addResting(limitOrder(
                1L, 200L, "AAPL", Side.SELL, "100.00", 2, Instant.parse("2026-03-13T10:00:00Z")
        ));
        book.addResting(limitOrder(
                2L, 201L, "AAPL", Side.SELL, "101.00", 1, Instant.parse("2026-03-13T10:00:05Z")
        ));

        BookOrder incomingBuy = marketOrder(
                3L, 100L, "AAPL", Side.BUY, 10, Instant.parse("2026-03-13T10:01:00Z")
        );

        List<TradeFill> fills = engine.match(book, incomingBuy);

        assertEquals(2, fills.size());
        assertEquals(3, fills.stream().mapToLong(TradeFill::quantity).sum());
        assertEquals(7, incomingBuy.openQty());
        assertEquals(OrderStatus.PARTIALLY_FILLED, incomingBuy.status());
        assertTrue(book.peekBest(Side.SELL).isEmpty());
    }

    private BookOrder limitOrder(long orderId,
                                 long traderId,
                                 String instrument,
                                 Side side,
                                 String price,
                                 long qty,
                                 Instant enteredAt) {
        return new BookOrder(
                orderId,
                traderId,
                instrument,
                side,
                new PriceIntent.Limit(new BigDecimal(price)),
                qty,
                enteredAt
        );
    }

    private BookOrder marketOrder(long orderId,
                                  long traderId,
                                  String instrument,
                                  Side side,
                                  long qty,
                                  Instant enteredAt) {
        return new BookOrder(
                orderId,
                traderId,
                instrument,
                side,
                new PriceIntent.Market(),
                qty,
                enteredAt
        );
    }
}