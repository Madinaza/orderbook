package com.aghaz.orderbook.trading.domain;

import java.math.BigDecimal;

public sealed interface PriceIntent permits PriceIntent.Market, PriceIntent.Limit {
    record Market() implements PriceIntent {}
    record Limit(BigDecimal limitPrice) implements PriceIntent {
        public Limit {
            if (limitPrice == null || limitPrice.signum() <= 0) {
                throw new IllegalArgumentException("Limit price must be positive.");
            }
        }
    }
}