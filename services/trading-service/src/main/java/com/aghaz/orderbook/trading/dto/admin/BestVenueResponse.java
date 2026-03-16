package com.aghaz.orderbook.trading.dto.admin;

import java.math.BigDecimal;
import java.util.List;

public record BestVenueResponse(
        String instrument,
        String side,
        long quantity,
        VenueDecision bestVenue,
        List<VenueDecision> evaluatedVenues
) {
    public record VenueDecision(
            String exchangeCode,
            String instrument,
            BigDecimal price,
            long availableQuantity,
            BigDecimal feeRate,
            BigDecimal totalNotional,
            BigDecimal feeAmount,
            BigDecimal effectiveValue
    ) {
    }
}