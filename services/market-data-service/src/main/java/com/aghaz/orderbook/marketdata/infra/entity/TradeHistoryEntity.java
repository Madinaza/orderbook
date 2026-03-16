package com.aghaz.orderbook.marketdata.infra.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "trade_history")
public class TradeHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique event id from Kafka.
     * We use it as an idempotency key so replays won’t duplicate rows.
     */
    @Column(name = "event_id", nullable = false, unique = true, length = 100)
    private String eventId;

    @Column(nullable = false, length = 16)
    private String instrument;

    @Column(name = "buy_order_id", nullable = false)
    private long buyOrderId;

    @Column(name = "sell_order_id", nullable = false)
    private long sellOrderId;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal price;

    @Column(nullable = false)
    private long quantity;

    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;
}