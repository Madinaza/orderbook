package com.aghaz.orderbook.trading.infra.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "trade_fill",
        indexes = {
                @Index(name = "idx_trade_fill_instrument_executed", columnList = "instrument, executed_at"),
                @Index(name = "idx_trade_fill_buy_trader_executed", columnList = "buy_trader_id, executed_at"),
                @Index(name = "idx_trade_fill_sell_trader_executed", columnList = "sell_trader_id, executed_at")
        }
)
public class TradeFillEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 16)
    private String instrument;

    @Column(name = "buy_order_id", nullable = false)
    private long buyOrderId;

    @Column(name = "sell_order_id", nullable = false)
    private long sellOrderId;

    @Column(name = "buy_trader_id", nullable = false)
    private long buyTraderId;

    @Column(name = "sell_trader_id", nullable = false)
    private long sellTraderId;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal price;

    @Column(nullable = false)
    private long quantity;

    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;

    public TradeFillEntity(String instrument,
                           long buyOrderId,
                           long sellOrderId,
                           long buyTraderId,
                           long sellTraderId,
                           BigDecimal price,
                           long quantity,
                           Instant executedAt) {
        this.instrument = instrument;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.buyTraderId = buyTraderId;
        this.sellTraderId = sellTraderId;
        this.price = price;
        this.quantity = quantity;
        this.executedAt = executedAt;
    }
}