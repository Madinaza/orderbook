package com.aghaz.orderbook.trading.infra.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "order_audit_log",
        indexes = {
                @Index(name = "idx_order_audit_log_order_created", columnList = "order_id, created_at")
        }
)
public class OrderAuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private long orderId;

    @Column(name = "trader_id", nullable = false)
    private long traderId;

    @Column(name = "event_type", nullable = false, length = 40)
    private String eventType;

    @Column(nullable = false, length = 255)
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public OrderAuditLogEntity(long orderId, long traderId, String eventType, String message, Instant createdAt) {
        this.orderId = orderId;
        this.traderId = traderId;
        this.eventType = eventType;
        this.message = message;
        this.createdAt = createdAt;
    }
}