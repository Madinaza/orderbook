package com.aghaz.orderbook.marketdata.app;

import com.aghaz.orderbook.marketdata.dto.OrderBookSnapshot;
import com.aghaz.orderbook.marketdata.infra.entity.OrderBookRowEntity;
import com.aghaz.orderbook.marketdata.infra.repo.OrderBookRowRepo;
import com.aghaz.orderbook.marketdata.infra.repo.TradeHistoryRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class MarketQueryServiceTest {

    @Mock
    private OrderBookRowRepo orderBookRowRepo;

    @Mock
    private TradeHistoryRepo tradeHistoryRepo;

    @InjectMocks
    private MarketQueryService marketQueryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void snapshot_shouldReturnNormalizedInstrument_andComputeBestBidAskSpread() {
        // Arrange
        String instrument = "aapl"; // lower-case on purpose to verify normalization
        String normalized = "AAPL";
        List<String> liveStatuses = List.of("NEW", "PARTIALLY_FILLED");

        OrderBookRowEntity bid = new OrderBookRowEntity();
        bid.setId(1L);
        bid.setTraderId(10L);
        bid.setInstrument(normalized);
        bid.setSide("BUY");
        bid.setOrderType("LIMIT");
        bid.setLimitPrice(new BigDecimal("100.00"));
        bid.setOriginalQty(10);
        bid.setOpenQty(10);
        bid.setStatus("NEW");
        bid.setCreatedAt(Instant.now());
        bid.setUpdatedAt(Instant.now());

        OrderBookRowEntity ask = new OrderBookRowEntity();
        ask.setId(2L);
        ask.setTraderId(11L);
        ask.setInstrument(normalized);
        ask.setSide("SELL");
        ask.setOrderType("LIMIT");
        ask.setLimitPrice(new BigDecimal("101.50"));
        ask.setOriginalQty(5);
        ask.setOpenQty(5);
        ask.setStatus("NEW");
        ask.setCreatedAt(Instant.now());
        ask.setUpdatedAt(Instant.now());

        when(orderBookRowRepo.findTop50ByInstrumentAndSideAndStatusInOrderByLimitPriceDescCreatedAtAsc(
                eq(normalized), eq("BUY"), eq(liveStatuses)
        )).thenReturn(List.of(bid));

        when(orderBookRowRepo.findTop50ByInstrumentAndSideAndStatusInOrderByLimitPriceAscCreatedAtAsc(
                eq(normalized), eq("SELL"), eq(liveStatuses)
        )).thenReturn(List.of(ask));

        // Act
        OrderBookSnapshot snapshot = marketQueryService.snapshot(instrument);

        // Assert
        assertEquals(normalized, snapshot.instrument());
        assertEquals(new BigDecimal("100.00"), snapshot.bestBid());
        assertEquals(new BigDecimal("101.50"), snapshot.bestAsk());
        assertEquals(new BigDecimal("1.50"), snapshot.spread());

        assertEquals(1, snapshot.bids().size());
        assertEquals(1, snapshot.asks().size());
        assertEquals(1L, snapshot.bids().get(0).id());
        assertEquals(2L, snapshot.asks().get(0).id());
    }

    @Test
    void snapshot_shouldThrowIfInstrumentBlank() {
        assertThrows(IllegalArgumentException.class, () -> marketQueryService.snapshot("  "));
    }
}