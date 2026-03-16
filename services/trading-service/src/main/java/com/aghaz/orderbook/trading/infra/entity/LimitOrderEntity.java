package com.aghaz.orderbook.trading.infra.entity;

import com.aghaz.orderbook.trading.domain.OrderStatus;
import com.aghaz.orderbook.trading.domain.OrderType;
import com.aghaz.orderbook.trading.domain.Side;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "limit_order",
        indexes = {
                @Index(name = "idx_limit_order_trader_created", columnList = "trader_id, created_at"),
                @Index(name = "idx_limit_order_instrument_status", columnList = "instrument, status")
        }
)
public class LimitOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_order_id", nullable = false, unique = true, length = 64)
    private String clientOrderId;

    @Column(name = "trader_id", nullable = false)
    private long traderId;

    @Column(nullable = false, length = 16)
    private String instrument;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Side side;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 16)
    private OrderType orderType;

    @Column(name = "limit_price", precision = 19, scale = 6)
    private BigDecimal limitPrice;

    @Column(name = "original_qty", nullable = false)
    private long originalQty;

    @Column(name = "open_qty", nullable = false)
    private long openQty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    public static LimitOrderEntity place(long traderId,
                                         String instrument,
                                         Side side,
                                         OrderType orderType,
                                         BigDecimal limitPrice,
                                         long quantity) {
        validateForCreate(instrument, side, orderType, limitPrice, quantity);

        LimitOrderEntity entity = new LimitOrderEntity();
        entity.clientOrderId = UUID.randomUUID().toString();
        entity.traderId = traderId;
        entity.instrument = instrument.trim().toUpperCase();
        entity.side = side;
        entity.orderType = orderType;
        entity.limitPrice = limitPrice;
        entity.originalQty = quantity;
        entity.openQty = quantity;
        entity.status = OrderStatus.NEW;
        entity.createdAt = Instant.now();
        entity.updatedAt = entity.createdAt;
        return entity;
    }

    public boolean isLive() {
        return status.isLive();
    }

    public long filledQty() {
        return originalQty - openQty;
    }

    public void cancel() {
        if (!isLive()) {
            if (status == OrderStatus.CANCELLED) {
                return;
            }
            throw new IllegalStateException("Only live orders can be cancelled.");
        }

        this.status = OrderStatus.CANCELLED;
        this.openQty = 0;
        touch();
    }

    /**
     * Real-world behavior:
     * replace is only valid for a live LIMIT order.
     * We preserve already-filled quantity and recalculate open quantity.
     */
    public void replace(BigDecimal newLimitPrice, long newTotalQuantity) {
        if (orderType != OrderType.LIMIT) {
            throw new IllegalStateException("Only LIMIT orders can be replaced.");
        }
        if (!isLive()) {
            throw new IllegalStateException("Only live orders can be replaced.");
        }
        if (newLimitPrice == null || newLimitPrice.signum() <= 0) {
            throw new IllegalArgumentException("New limit price must be positive.");
        }
        if (newTotalQuantity <= 0) {
            throw new IllegalArgumentException("New quantity must be greater than zero.");
        }

        long alreadyFilled = filledQty();
        if (newTotalQuantity < alreadyFilled) {
            throw new IllegalArgumentException(
                    "New quantity cannot be less than already filled quantity (" + alreadyFilled + ")."
            );
        }

        this.limitPrice = newLimitPrice;
        this.originalQty = newTotalQuantity;
        this.openQty = newTotalQuantity - alreadyFilled;
        this.status = (openQty == 0) ? OrderStatus.FILLED : (alreadyFilled == 0 ? OrderStatus.NEW : OrderStatus.PARTIALLY_FILLED);
        touch();
    }

    public void applyFill(long fillQty) {
        if (fillQty <= 0) {
            return;
        }
        if (!isLive()) {
            throw new IllegalStateException("Order is not live.");
        }
        if (fillQty > openQty) {
            throw new IllegalArgumentException("Fill quantity exceeds open quantity.");
        }

        this.openQty -= fillQty;
        this.status = (this.openQty == 0) ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED;
        touch();
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    private static void validateForCreate(String instrument,
                                          Side side,
                                          OrderType orderType,
                                          BigDecimal limitPrice,
                                          long quantity) {
        if (instrument == null || instrument.isBlank()) {
            throw new IllegalArgumentException("Instrument is required.");
        }

        Objects.requireNonNull(side, "Side is required.");
        Objects.requireNonNull(orderType, "Order type is required.");

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        if (orderType.requiresLimitPrice()) {
            if (limitPrice == null || limitPrice.signum() <= 0) {
                throw new IllegalArgumentException("Limit price must be positive for LIMIT orders.");
            }
        }
    }
}