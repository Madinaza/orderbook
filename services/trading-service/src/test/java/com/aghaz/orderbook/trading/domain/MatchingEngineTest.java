package com.aghaz.orderbook.trading.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MatchingEngineTest {

    @Test
    void limitBuy_shouldMatchBestAsk_fifo() {
        OrderBook book = new OrderBook("AAPL");
        MatchingEngine engine = new MatchingEngine();

        BookOrder sell1 = new BookOrder(
                1, 200, "AAPL", Side.SELL,
                new PriceIntent.Limit(new BigDecimal("101.00")),
                5, Instant.now()
        );
        book.addResting(sell1);

        BookOrder buy = new BookOrder(
                2, 100, "AAPL", Side.BUY,
                new PriceIntent.Limit(new BigDecimal("101.00")),
                3, Instant.now()
        );

        List<TradeFill> fills = engine.match(book, buy);

        assertEquals(1, fills.size());
        assertEquals(3, fills.get(0).quantity());
        assertEquals(new BigDecimal("101.00"), fills.get(0).price());
        assertEquals(0, buy.openQty());
        assertEquals(OrderStatus.FILLED, buy.status());
        assertEquals(2, sell1.openQty());
        assertEquals(OrderStatus.PARTIALLY_FILLED, sell1.status());
    }

    @Test
    void marketOrder_shouldConsumeUntilBookEmpty() {
        OrderBook book = new OrderBook("AAPL");
        MatchingEngine engine = new MatchingEngine();

        book.addResting(new BookOrder(
                1, 200, "AAPL", Side.SELL,
                new PriceIntent.Limit(new BigDecimal("101.00")),
                2, Instant.now()
        ));

        book.addResting(new BookOrder(
                2, 201, "AAPL", Side.SELL,
                new PriceIntent.Limit(new BigDecimal("102.00")),
                2, Instant.now()
        ));

        BookOrder buyMarket = new BookOrder(
                3, 100, "AAPL", Side.BUY,
                new PriceIntent.Market(),
                10, Instant.now()
        );

        List<TradeFill> fills = engine.match(book, buyMarket);

        assertEquals(2, fills.size());
        assertEquals(4, fills.stream().mapToLong(TradeFill::quantity).sum());
        assertEquals(6, buyMarket.openQty());
    }
}