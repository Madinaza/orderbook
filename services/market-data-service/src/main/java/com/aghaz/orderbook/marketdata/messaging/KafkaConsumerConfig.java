package com.aghaz.orderbook.marketdata.messaging;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer configuration for market-data-service.
 *
 * Important:
 * We configure JsonDeserializer programmatically here.
 * Do not also configure Spring JSON deserializer behavior in application.yml,
 * otherwise Spring Kafka throws:
 * "JsonDeserializer must be configured with property setters, or via configuration properties; not both"
 */
@Configuration
public class KafkaConsumerConfig {

    @Bean
    ConsumerFactory<String, OrderChangedEvent> orderChangedConsumerFactory(KafkaProperties props) {
        Map<String, Object> config = new HashMap<>(props.buildConsumerProperties());

        JsonDeserializer<OrderChangedEvent> deserializer = new JsonDeserializer<>(OrderChangedEvent.class);
        deserializer.addTrustedPackages("com.aghaz.orderbook.shared_contracts.messaging");
        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, OrderChangedEvent> orderChangedKafkaListenerFactory(
            ConsumerFactory<String, OrderChangedEvent> cf
    ) {
        ConcurrentKafkaListenerContainerFactory<String, OrderChangedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }

    @Bean
    ConsumerFactory<String, TradeExecutedEvent> tradeExecutedConsumerFactory(KafkaProperties props) {
        Map<String, Object> config = new HashMap<>(props.buildConsumerProperties());

        JsonDeserializer<TradeExecutedEvent> deserializer = new JsonDeserializer<>(TradeExecutedEvent.class);
        deserializer.addTrustedPackages("com.aghaz.orderbook.shared_contracts.messaging");
        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, TradeExecutedEvent> tradeExecutedKafkaListenerFactory(
            ConsumerFactory<String, TradeExecutedEvent> cf
    ) {
        ConcurrentKafkaListenerContainerFactory<String, TradeExecutedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }
}