package com.aghaz.orderbook.marketdata.messaging;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Emitted by trading-service per execution.
 * Market-data-service stores it verbatim for history queries.
 */
public record TradeExecutedEvent(
        String eventId,
        String instrument,
        long buyOrderId,
        long sellOrderId,
        BigDecimal price,
        long quantity,
        Instant executedAt
) {}