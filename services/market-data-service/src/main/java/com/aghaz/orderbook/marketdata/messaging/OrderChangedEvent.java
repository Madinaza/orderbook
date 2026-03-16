package com.aghaz.orderbook.marketdata.messaging;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Emitted by trading-service whenever an order changes state.
 * This is the read-model’s source of truth.
 */
public record OrderChangedEvent(
        String eventId,
        Long orderId,
        long traderId,
        String instrument,
        String side,
        String orderType,
        BigDecimal limitPrice,
        long originalQty,
        long openQty,
        String status,
        Instant createdAt,
        Instant updatedAt
) {}