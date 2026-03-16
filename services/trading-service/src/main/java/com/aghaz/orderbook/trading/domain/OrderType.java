package com.aghaz.orderbook.trading.domain;

public enum OrderType {
    MARKET,
    LIMIT;

    public boolean requiresLimitPrice() {
        return this == LIMIT;
    }

    public boolean isMarket() {
        return this == MARKET;
    }
}