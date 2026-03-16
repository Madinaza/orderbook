package com.aghaz.orderbook.marketdata.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderBookSnapshot(
        String instrument,
        BigDecimal bestBid,
        BigDecimal bestAsk,
        BigDecimal spread,
        List<OrderRow> bids,
        List<OrderRow> asks
) {}