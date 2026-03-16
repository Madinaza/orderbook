package com.aghaz.orderbook.shared_contracts.messaging;

import java.math.BigDecimal;
import java.time.Instant;

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