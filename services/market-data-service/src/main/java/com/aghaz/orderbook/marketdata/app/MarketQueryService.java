package com.aghaz.orderbook.marketdata.app;

import com.aghaz.orderbook.marketdata.dto.OrderBookSnapshot;
import com.aghaz.orderbook.marketdata.dto.OrderRow;
import com.aghaz.orderbook.marketdata.dto.TradeHistoryRow;
import com.aghaz.orderbook.marketdata.infra.repo.OrderBookRowRepo;
import com.aghaz.orderbook.marketdata.infra.repo.TradeHistoryRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MarketQueryService {

    private static final String LIVE_1 = "NEW";
    private static final String LIVE_2 = "PARTIALLY_FILLED";

    private final OrderBookRowRepo orderRows;
    private final TradeHistoryRepo tradeHistory;

    public MarketQueryService(OrderBookRowRepo orderRows, TradeHistoryRepo tradeHistory) {
        this.orderRows = orderRows;
        this.tradeHistory = tradeHistory;
    }

    public OrderBookSnapshot snapshot(String instrument) {
        String symbol = normalizeInstrument(instrument);
        List<String> liveStatuses = List.of(LIVE_1, LIVE_2);

        var bids = orderRows
                .findTop50ByInstrumentAndSideAndStatusInOrderByLimitPriceDescCreatedAtAsc(
                        symbol, "BUY", liveStatuses
                )
                .stream()
                .map(e -> new OrderRow(
                        e.getId(),
                        e.getTraderId(),
                        e.getInstrument(),
                        e.getSide(),
                        e.getOrderType(),
                        e.getLimitPrice(),
                        e.getOpenQty(),
                        e.getStatus(),
                        e.getCreatedAt()
                ))
                .toList();

        var asks = orderRows
                .findTop50ByInstrumentAndSideAndStatusInOrderByLimitPriceAscCreatedAtAsc(
                        symbol, "SELL", liveStatuses
                )
                .stream()
                .map(e -> new OrderRow(
                        e.getId(),
                        e.getTraderId(),
                        e.getInstrument(),
                        e.getSide(),
                        e.getOrderType(),
                        e.getLimitPrice(),
                        e.getOpenQty(),
                        e.getStatus(),
                        e.getCreatedAt()
                ))
                .toList();

        BigDecimal bestBid = bids.isEmpty() ? null : bids.get(0).limitPrice();
        BigDecimal bestAsk = asks.isEmpty() ? null : asks.get(0).limitPrice();
        BigDecimal spread = (bestBid != null && bestAsk != null) ? bestAsk.subtract(bestBid) : null;

        return new OrderBookSnapshot(symbol, bestBid, bestAsk, spread, bids, asks);
    }

    public List<TradeHistoryRow> trades(String instrument) {
        String symbol = normalizeInstrument(instrument);

        return tradeHistory.findTop100ByInstrumentOrderByExecutedAtDesc(symbol)
                .stream()
                .map(t -> new TradeHistoryRow(
                        t.getBuyOrderId(),
                        t.getSellOrderId(),
                        t.getPrice(),
                        t.getQuantity(),
                        t.getExecutedAt()
                ))
                .toList();
    }

    private String normalizeInstrument(String instrument) {
        if (instrument == null || instrument.isBlank()) {
            throw new IllegalArgumentException("Instrument is required");
        }
        return instrument.trim().toUpperCase();
    }
}