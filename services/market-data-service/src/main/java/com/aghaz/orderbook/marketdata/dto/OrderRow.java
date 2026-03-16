package com.aghaz.orderbook.marketdata.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderRow(
        Long id,
        long traderId,
        String instrument,
        String side,
        String orderType,
        BigDecimal limitPrice,
        long openQty,
        String status,
        Instant createdAt
) {}