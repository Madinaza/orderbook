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
@Table(name = "order_book_row")
public class OrderBookRowEntity {

    @Id
    private Long id; // orderId from trading-service

    @Column(name = "trader_id", nullable = false)
    private long traderId;

    @Column(nullable = false, length = 16)
    private String instrument;

    @Column(nullable = false, length = 8)
    private String side;

    @Column(name = "order_type", nullable = false, length = 16)
    private String orderType;

    @Column(name = "limit_price", precision = 19, scale = 6)
    private BigDecimal limitPrice;

    @Column(name = "original_qty", nullable = false)
    private long originalQty;

    @Column(name = "open_qty", nullable = false)
    private long openQty;

    @Column(nullable = false, length = 24)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Custom constructor (if needed)
    public OrderBookRowEntity(Long id, long traderId, String instrument, String side, String orderType,
                              BigDecimal limitPrice, long originalQty, long openQty, String status,
                              Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.traderId = traderId;
        this.instrument = instrument;
        this.side = side;
        this.orderType = orderType;
        this.limitPrice = limitPrice;
        this.originalQty = originalQty;
        this.openQty = openQty;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}