package com.aghaz.orderbook.trading.messaging;

import com.aghaz.orderbook.shared_contracts.messaging.OrderChangedEvent;
import com.aghaz.orderbook.shared_contracts.messaging.TradeExecutedEvent;

public interface TradingEventPublisher {
    void orderChanged(OrderChangedEvent event);
    void tradeExecuted(TradeExecutedEvent event);
}