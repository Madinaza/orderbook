package com.aghaz.orderbook.trading.dto.admin;

import java.math.BigDecimal;
import java.util.List;

public record AdminDashboardResponse(
        long totalOrders,
        long openOrders,
        long filledOrders,
        long cancelledOrders,
        long totalTrades,
        long activeInstruments,
        BigDecimal buySellRatio,
        BigDecimal filledRatePercent,
        BigDecimal cancelRatePercent,
        List<InstrumentSummaryRow> instruments,
        List<InstrumentSummaryRow> top5ActiveInstruments,
        List<InstrumentSummaryRow> top5TradedInstruments
) {}