package com.aghaz.orderbook.marketdata.realtime;

import com.aghaz.orderbook.marketdata.dto.OrderBookSnapshot;
import com.aghaz.orderbook.marketdata.dto.TradeHistoryRow;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MarketDataBroadcaster {

    private final SimpMessagingTemplate ws;

    public MarketDataBroadcaster(SimpMessagingTemplate ws) {
        this.ws = ws;
    }

    public void pushOrderBook(String instrument, OrderBookSnapshot snapshot) {
        ws.convertAndSend("/topic/orderbook/" + instrument, snapshot);
    }

    public void pushTrades(String instrument, List<TradeHistoryRow> trades) {
        ws.convertAndSend("/topic/trades/" + instrument, trades);
    }
}