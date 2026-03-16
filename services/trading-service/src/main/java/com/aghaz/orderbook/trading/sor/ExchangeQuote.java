package com.aghaz.orderbook.trading.sor;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * One venue quote snapshot used by the Smart Order Router.
 *
 * Example:
 * - exchangeCode = NYSE
 * - instrument = AAPL
 * - price = 100.25
 * - availableQuantity = 500
 * - feeRate = 0.0015 (0.15%)
 */
public record ExchangeQuote(
        String exchangeCode,
        String instrument,
        BigDecimal price,
        long availableQuantity,
        BigDecimal feeRate
) {
    public ExchangeQuote {
        exchangeCode = normalizeRequired(exchangeCode, "exchangeCode");
        instrument = normalizeRequired(instrument, "instrument");

        Objects.requireNonNull(price, "price must not be null");
        Objects.requireNonNull(feeRate, "feeRate must not be null");

        if (price.signum() <= 0) {
            throw new IllegalArgumentException("price must be positive");
        }

        if (availableQuantity <= 0) {
            throw new IllegalArgumentException("availableQuantity must be greater than zero");
        }

        if (feeRate.signum() < 0) {
            throw new IllegalArgumentException("feeRate must not be negative");
        }
    }

    private static String normalizeRequired(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim().toUpperCase();
    }
}