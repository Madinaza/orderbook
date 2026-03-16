package com.aghaz.orderbook.trading.messaging;

import com.aghaz.orderbook.shared_contracts.messaging.EventTopics;
import com.aghaz.orderbook.shared_contracts.messaging.OrderChangedEvent;
import com.aghaz.orderbook.shared_contracts.messaging.TradeExecutedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Real Kafka-backed publisher for trading domain events.
 */
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaTradingEventPublisher implements TradingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaTradingEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaTradingEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void orderChanged(OrderChangedEvent event) {
        try {
            kafkaTemplate.send(EventTopics.ORDER_CHANGED, String.valueOf(event.orderId()), event);
            log.info("Published orderChanged event. orderId={}, eventId={}", event.orderId(), event.eventId());
        } catch (Exception ex) {
            log.error("Failed to publish orderChanged event. orderId={}, eventId={}", event.orderId(), event.eventId(), ex);
            throw ex;
        }
    }

    @Override
    public void tradeExecuted(TradeExecutedEvent event) {
        try {
            kafkaTemplate.send(EventTopics.TRADE_EXECUTED, event.instrument(), event);
            log.info("Published tradeExecuted event. instrument={}, eventId={}", event.instrument(), event.eventId());
        } catch (Exception ex) {
            log.error("Failed to publish tradeExecuted event. instrument={}, eventId={}", event.instrument(), event.eventId(), ex);
            throw ex;
        }
    }
}