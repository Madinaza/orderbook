package com.aghaz.orderbook.trading.domain;

public enum OrderStatus {
    NEW,
    PARTIALLY_FILLED,
    FILLED,
    CANCELLED,
    REJECTED;

    public boolean isTerminal() {
        return this == FILLED || this == CANCELLED || this == REJECTED;
    }

    public boolean isLive() {
        return this == NEW || this == PARTIALLY_FILLED;
    }
}