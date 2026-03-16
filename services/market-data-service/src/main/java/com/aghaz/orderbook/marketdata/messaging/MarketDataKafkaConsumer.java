package com.aghaz.orderbook.marketdata.messaging;

import com.aghaz.orderbook.marketdata.app.MarketProjectionService;
import com.aghaz.orderbook.marketdata.app.MarketQueryService;
import com.aghaz.orderbook.marketdata.realtime.MarketDataBroadcaster;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class MarketDataKafkaConsumer {

    private final MarketProjectionService projections;
    private final MarketQueryService queries;
    private final MarketDataBroadcaster broadcaster;

    public MarketDataKafkaConsumer(MarketProjectionService projections,
                                   MarketQueryService queries,
                                   MarketDataBroadcaster broadcaster) {
        this.projections = projections;
        this.queries = queries;
        this.broadcaster = broadcaster;
    }

    @KafkaListener(topics = EventTopics.ORDER_CHANGED, groupId = "market-data-service",
            containerFactory = "orderChangedKafkaListenerFactory")
    public void onOrderChanged(OrderChangedEvent event) {
        projections.apply(event);
        String symbol = event.instrument().trim().toUpperCase();
        broadcaster.pushOrderBook(symbol, queries.snapshot(symbol));
    }

    @KafkaListener(topics = EventTopics.TRADE_EXECUTED, groupId = "market-data-service",
            containerFactory = "tradeExecutedKafkaListenerFactory")
    public void onTradeExecuted(TradeExecutedEvent event) {
        projections.apply(event);
        String symbol = event.instrument().trim().toUpperCase();
        broadcaster.pushTrades(symbol, queries.trades(symbol));
    }
}