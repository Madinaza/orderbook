package com.aghaz.orderbook.trading.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable domain event/result representing one execution between a buy and a sell order.
 * Contains enough information to persist trade history and publish integration events
 * without additional database lookups.
 */
public record TradeFill(
        String instrument,
        long buyOrderId,
        long sellOrderId,
        long buyTraderId,
        long sellTraderId,
        BigDecimal price,
        long quantity,
        Instant executedAt
) {
    public static TradeFill now(String instrument,
                                long buyOrderId,
                                long sellOrderId,
                                long buyTraderId,
                                long sellTraderId,
                                BigDecimal price,
                                long quantity) {
        return new TradeFill(
                instrument,
                buyOrderId,
                sellOrderId,
                buyTraderId,
                sellTraderId,
                price,
                quantity,
                Instant.now()
        );
    }
}