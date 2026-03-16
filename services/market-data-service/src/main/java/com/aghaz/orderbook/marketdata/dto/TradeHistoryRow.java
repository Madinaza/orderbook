package com.aghaz.orderbook.marketdata.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeHistoryRow(
        long buyOrderId,
        long sellOrderId,
        BigDecimal price,
        long quantity,
        Instant executedAt
) {}