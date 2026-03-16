package com.aghaz.orderbook.shared_contracts.messaging;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeExecutedEvent(
        String eventId,
        String instrument,
        long buyOrderId,
        long sellOrderId,
        BigDecimal price,
        long quantity,
        Instant executedAt
) {}