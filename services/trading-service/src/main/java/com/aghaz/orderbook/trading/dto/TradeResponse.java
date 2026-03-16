package com.aghaz.orderbook.trading.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeResponse(
        Long id,
        String instrument,
        long buyOrderId,
        long sellOrderId,
        long buyTraderId,
        long sellTraderId,
        BigDecimal price,
        long quantity,
        Instant executedAt,
        String mySide
) {}