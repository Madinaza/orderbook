package com.aghaz.orderbook.trading.dto;

import com.aghaz.orderbook.trading.domain.OrderType;
import com.aghaz.orderbook.trading.domain.Side;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PlaceOrderRequest(
        @NotBlank String instrument,
        @NotNull Side side,
        @NotNull OrderType orderType,
        @DecimalMin(value = "0.000001", inclusive = true) BigDecimal limitPrice,
        @Min(1) long quantity
) {}