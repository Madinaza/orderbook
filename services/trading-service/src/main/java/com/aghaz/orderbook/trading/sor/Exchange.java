package com.aghaz.orderbook.trading.sor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public final class Exchange {

    private final String code;
    private final Map<Long, BigDecimal> feeLadderByVolume;

    public Exchange(String code, Map<Long, BigDecimal> feeLadderByVolume) {
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.feeLadderByVolume = Map.copyOf(feeLadderByVolume);
    }

    public String code() {
        return code;
    }

    public BigDecimal feeRateFor(long quantity) {
        return feeLadderByVolume.entrySet().stream()
                .filter(entry -> quantity >= entry.getKey())
                .max(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElse(BigDecimal.ZERO);
    }
}