package com.aghaz.orderbook.trading.dto.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.List;

public record BestVenueRequest(
        @NotBlank String instrument,
        @NotBlank String side,
        @Min(1) long quantity,
        @NotEmpty List<@Valid VenueQuoteInput> quotes
) {
    public record VenueQuoteInput(
            @NotBlank String exchangeCode,
            @NotBlank String instrument,
            @jakarta.validation.constraints.DecimalMin(value = "0.000001", inclusive = true)
            BigDecimal price,
            @Min(1) long availableQuantity,
            @jakarta.validation.constraints.DecimalMin(value = "0.0", inclusive = true)
            BigDecimal feeRate
    ) {
    }
}