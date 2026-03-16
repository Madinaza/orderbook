package com.aghaz.orderbook.marketdata.messaging;

/**
 * Central place for topic names.
 * Avoids “stringly-typed” Kafka integration across the codebase.
 */
public final class EventTopics {
    private EventTopics() {}

    public static final String ORDER_CHANGED = "order.changed";
    public static final String TRADE_EXECUTED = "trade.executed";
}