package com.aghaz.orderbook.trading.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ReplaceOrderRequest(
        @NotNull @DecimalMin(value = "0.000001", inclusive = true) BigDecimal newLimitPrice,
        @Min(1) long newQuantity
) {}