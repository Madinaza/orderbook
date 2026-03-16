package com.aghaz.orderbook.marketdata.api;

import com.aghaz.orderbook.marketdata.app.MarketQueryService;
import com.aghaz.orderbook.marketdata.dto.OrderBookSnapshot;
import com.aghaz.orderbook.marketdata.dto.TradeHistoryRow;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MarketDataController {

    private final MarketQueryService queries;

    public MarketDataController(MarketQueryService queries) {
        this.queries = queries;
    }

    @GetMapping("/orderbook/{instrument}")
    public OrderBookSnapshot orderBook(@PathVariable String instrument) {
        return queries.snapshot(instrument);
    }

    @GetMapping("/trades/{instrument}")
    public List<TradeHistoryRow> trades(@PathVariable String instrument) {
        return queries.trades(instrument);
    }
}