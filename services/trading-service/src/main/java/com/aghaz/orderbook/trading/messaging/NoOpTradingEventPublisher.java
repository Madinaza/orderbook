package com.aghaz.orderbook.trading.messaging;

import com.aghaz.orderbook.shared_contracts.messaging.OrderChangedEvent;
import com.aghaz.orderbook.shared_contracts.messaging.TradeExecutedEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpTradingEventPublisher implements TradingEventPublisher {

    @Override
    public void orderChanged(OrderChangedEvent event) {
        // no-op for local/test mode
    }

    @Override
    public void tradeExecuted(TradeExecutedEvent event) {
        // no-op for local/test mode
    }
}