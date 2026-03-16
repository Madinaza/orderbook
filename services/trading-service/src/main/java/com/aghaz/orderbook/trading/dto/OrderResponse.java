package com.aghaz.orderbook.trading.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        Long id,
        String clientOrderId,
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