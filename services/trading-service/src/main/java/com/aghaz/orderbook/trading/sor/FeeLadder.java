package com.aghaz.orderbook.trading.sor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Encapsulates fee calculations for routing decisions.
 */
public final class FeeLadder {

    private static final int MONEY_SCALE = 6;

    public BigDecimal feeAmount(BigDecimal notional, BigDecimal feeRate) {
        Objects.requireNonNull(notional, "notional must not be null");
        Objects.requireNonNull(feeRate, "feeRate must not be null");

        if (notional.signum() < 0) {
            throw new IllegalArgumentException("notional must not be negative");
        }

        if (feeRate.signum() < 0) {
            throw new IllegalArgumentException("feeRate must not be negative");
        }

        return notional.multiply(feeRate).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal effectiveBuyCost(BigDecimal price, long quantity, BigDecimal feeRate) {
        BigDecimal notional = notional(price, quantity);
        return notional.add(feeAmount(notional, feeRate)).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal effectiveSellProceeds(BigDecimal price, long quantity, BigDecimal feeRate) {
        BigDecimal notional = notional(price, quantity);
        return notional.subtract(feeAmount(notional, feeRate)).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal notional(BigDecimal price, long quantity) {
        Objects.requireNonNull(price, "price must not be null");

        if (price.signum() <= 0) {
            throw new IllegalArgumentException("price must be positive");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }

        return price.multiply(BigDecimal.valueOf(quantity)).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}