package com.aghaz.orderbook.trading.domain;

import java.time.Instant;
import java.util.Objects;

public final class BookOrder {
    private final long orderId;
    private final long traderId;
    private final String instrument;
    private final Side side;
    private final PriceIntent priceIntent;
    private final long originalQty;
    private final Instant enteredAt;

    private long openQty;
    private OrderStatus status = OrderStatus.NEW;

    public BookOrder(long orderId,
                     long traderId,
                     String instrument,
                     Side side,
                     PriceIntent priceIntent,
                     long qty,
                     Instant enteredAt) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0.");
        }

        this.orderId = orderId;
        this.traderId = traderId;
        this.instrument = Objects.requireNonNull(instrument, "instrument must not be null");
        this.side = Objects.requireNonNull(side, "side must not be null");
        this.priceIntent = Objects.requireNonNull(priceIntent, "priceIntent must not be null");
        this.originalQty = qty;
        this.openQty = qty;
        this.enteredAt = Objects.requireNonNull(enteredAt, "enteredAt must not be null");
    }

    public long id() {
        return orderId;
    }

    public long traderId() {
        return traderId;
    }

    public String instrument() {
        return instrument;
    }

    public Side side() {
        return side;
    }

    public PriceIntent priceIntent() {
        return priceIntent;
    }

    public long originalQty() {
        return originalQty;
    }

    public long openQty() {
        return openQty;
    }

    public Instant enteredAt() {
        return enteredAt;
    }

    public OrderStatus status() {
        return status;
    }

    public boolean isLive() {
        return status == OrderStatus.NEW || status == OrderStatus.PARTIALLY_FILLED;
    }

    public void applyFill(long fillQty) {
        if (fillQty <= 0) {
            return;
        }
        if (!isLive()) {
            throw new IllegalStateException("Order is not live.");
        }
        if (fillQty > openQty) {
            throw new IllegalArgumentException("Fill exceeds open quantity.");
        }

        openQty -= fillQty;
        status = (openQty == 0) ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED;
    }
}