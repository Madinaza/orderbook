package com.aghaz.orderbook.trading.app;

import com.aghaz.orderbook.trading.domain.OrderStatus;
import com.aghaz.orderbook.trading.dto.admin.AdminDashboardResponse;
import com.aghaz.orderbook.trading.infra.repo.LimitOrderRepo;
import com.aghaz.orderbook.trading.infra.repo.TradeFillRepo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AdminDashboardQueryServiceTest {

    @Test
    void summary_shouldCalculateAdvancedMetrics() {
        LimitOrderRepo limitOrderRepo = mock(LimitOrderRepo.class);
        TradeFillRepo tradeFillRepo = mock(TradeFillRepo.class);

        when(limitOrderRepo.count()).thenReturn(10L);
        when(limitOrderRepo.countByStatus(OrderStatus.NEW)).thenReturn(2L);
        when(limitOrderRepo.countByStatus(OrderStatus.PARTIALLY_FILLED)).thenReturn(1L);
        when(limitOrderRepo.countByStatus(OrderStatus.FILLED)).thenReturn(5L);
        when(limitOrderRepo.countByStatus(OrderStatus.CANCELLED)).thenReturn(2L);
        when(tradeFillRepo.count()).thenReturn(7L);
        when(limitOrderRepo.countDistinctInstruments()).thenReturn(3L);
        when(limitOrderRepo.countBuyOrders()).thenReturn(6L);
        when(limitOrderRepo.countSellOrders()).thenReturn(4L);

        when(limitOrderRepo.countOrdersByInstrument()).thenReturn(List.of(
                new Object[]{"AAPL", 5L},
                new Object[]{"MSFT", 3L}
        ));

        when(tradeFillRepo.countTradesByInstrument()).thenReturn(List.of(
                new Object[]{"AAPL", 4L},
                new Object[]{"MSFT", 2L}
        ));

        when(limitOrderRepo.sumOpenQuantityByInstrument()).thenReturn(List.of(
                new Object[]{"AAPL", 120L},
                new Object[]{"MSFT", 50L}
        ));

        AdminDashboardQueryService service = new AdminDashboardQueryService(limitOrderRepo, tradeFillRepo);

        AdminDashboardResponse response = service.summary();

        assertEquals(10L, response.totalOrders());
        assertEquals(3L, response.openOrders());
        assertEquals(5L, response.filledOrders());
        assertEquals(2L, response.cancelledOrders());
        assertEquals(7L, response.totalTrades());
        assertEquals(3L, response.activeInstruments());
        assertEquals(new BigDecimal("1.50"), response.buySellRatio());
        assertEquals(new BigDecimal("50.00"), response.filledRatePercent());
        assertEquals(new BigDecimal("20.00"), response.cancelRatePercent());
        assertEquals(2, response.top5ActiveInstruments().size());
        assertEquals("AAPL", response.top5ActiveInstruments().get(0).instrument());
    }
}