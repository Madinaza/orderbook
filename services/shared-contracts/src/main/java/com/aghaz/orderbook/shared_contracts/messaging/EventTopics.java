package com.aghaz.orderbook.shared_contracts.messaging;

public final class EventTopics {
    private EventTopics() {}

    public static final String ORDER_CHANGED = "order.changed";
    public static final String TRADE_EXECUTED = "trade.executed";
}