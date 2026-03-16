package com.aghaz.orderbook.trading.api;

import com.aghaz.orderbook.shared_contracts.api.PagedResponse;
import com.aghaz.orderbook.trading.app.TradeQueryService;
import com.aghaz.orderbook.trading.dto.TradeResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeQueryService tradeQueryService;

    public TradeController(TradeQueryService tradeQueryService) {
        this.tradeQueryService = tradeQueryService;
    }

    @GetMapping("/mine")
    public PagedResponse<TradeResponse> myTrades(
            Authentication auth,
            @RequestParam(required = false) String instrument,
            @RequestParam(required = false) String side,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "executedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        return tradeQueryService.myTrades(
                traderId(auth),
                instrument,
                from,
                to,
                side,
                page,
                size,
                sortBy,
                sortDirection
        );
    }

    @GetMapping("/{instrument}")
    public PagedResponse<TradeResponse> byInstrument(
            @PathVariable String instrument,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "executedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        return tradeQueryService.byInstrument(instrument, page, size, sortBy, sortDirection);
    }

    private long traderId(Authentication auth) {
        Object p = auth.getPrincipal();
        if (p instanceof Long l) return l;
        if (p instanceof Integer i) return i.longValue();
        if (p instanceof String s) return Long.parseLong(s);
        throw new IllegalStateException("Unexpected principal type: " + p);
    }
}