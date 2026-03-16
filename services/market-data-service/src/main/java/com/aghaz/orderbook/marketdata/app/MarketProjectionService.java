package com.aghaz.orderbook.marketdata.app;

import com.aghaz.orderbook.marketdata.infra.entity.OrderBookRowEntity;
import com.aghaz.orderbook.marketdata.infra.entity.TradeHistoryEntity;
import com.aghaz.orderbook.marketdata.infra.repo.OrderBookRowRepo;
import com.aghaz.orderbook.marketdata.infra.repo.TradeHistoryRepo;
import com.aghaz.orderbook.marketdata.messaging.OrderChangedEvent;
import com.aghaz.orderbook.marketdata.messaging.TradeExecutedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketProjectionService {

    private final OrderBookRowRepo orderRows;
    private final TradeHistoryRepo tradeHistory;

    public MarketProjectionService(OrderBookRowRepo orderRows, TradeHistoryRepo tradeHistory) {
        this.orderRows = orderRows;
        this.tradeHistory = tradeHistory;
    }

    /**
     * OrderChangedEvent is the single source of truth for the order-book projection.
     * We store the latest state by orderId.
     */
    @Transactional
    public void apply(OrderChangedEvent e) {
        OrderBookRowEntity row = orderRows.findById(e.orderId()).orElseGet(OrderBookRowEntity::new);
        row.setId(e.orderId());
        row.setTraderId(e.traderId());
        row.setInstrument(normalizeInstrument(e.instrument()));
        row.setSide(e.side());
        row.setOrderType(e.orderType());
        row.setLimitPrice(e.limitPrice());
        row.setOriginalQty(e.originalQty());
        row.setOpenQty(e.openQty());
        row.setStatus(e.status());
        row.setCreatedAt(e.createdAt());
        row.setUpdatedAt(e.updatedAt());
        orderRows.save(row);
    }

    /**
     * Trades are append-only. We guard against duplicates using eventId.
     * This keeps replays safe (a common Kafka reality).
     */
    @Transactional
    public void apply(TradeExecutedEvent e) {
        if (tradeHistory.findByEventId(e.eventId()).isPresent()) return;

        TradeHistoryEntity t = new TradeHistoryEntity();
        t.setEventId(e.eventId());
        t.setInstrument(normalizeInstrument(e.instrument()));
        t.setBuyOrderId(e.buyOrderId());
        t.setSellOrderId(e.sellOrderId());
        t.setPrice(e.price());
        t.setQuantity(e.quantity());
        t.setExecutedAt(e.executedAt());

        tradeHistory.save(t);
    }

    private String normalizeInstrument(String instrument) {
        return instrument == null ? null : instrument.trim().toUpperCase();
    }
}