package com.aghaz.orderbook.trading.dto.admin;

public record InstrumentSummaryRow(
        String instrument,
        long orders,
        long trades,
        long openQuantity
) {}