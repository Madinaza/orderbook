package com.aghaz.orderbook.trading.app;

import com.aghaz.orderbook.trading.domain.OrderType;
import com.aghaz.orderbook.trading.domain.Side;
import com.aghaz.orderbook.trading.dto.PlaceOrderRequest;
import com.aghaz.orderbook.trading.infra.entity.LimitOrderEntity;
import com.aghaz.orderbook.trading.infra.entity.TradeFillEntity;
import com.aghaz.orderbook.trading.infra.repo.LimitOrderRepo;
import com.aghaz.orderbook.trading.infra.repo.TradeFillRepo;
import com.aghaz.orderbook.trading.messaging.TradingEventPublisher;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OrderMatchingIntegrationTest {

    @Test
    void place_shouldMatchIncomingBuyAgainstExistingSell() {
        LimitOrderRepo orderRepo = mock(LimitOrderRepo.class);
        TradeFillRepo fillRepo = mock(TradeFillRepo.class);
        TradingEventPublisher publisher = mock(TradingEventPublisher.class);
        OrderAuditService orderAuditService = mock(OrderAuditService.class);

        OrderCommandService service =
                new OrderCommandService(orderRepo, fillRepo, publisher, orderAuditService);

        AtomicLong ids = new AtomicLong(100);

        LimitOrderEntity restingSell = LimitOrderEntity.place(
                2L, "AAPL", Side.SELL, OrderType.LIMIT, new BigDecimal("100.00"), 5
        );
        setField(restingSell, "id", 10L);
        setField(restingSell, "createdAt", Instant.now());
        setField(restingSell, "updatedAt", Instant.now());

        when(orderRepo.save(any(LimitOrderEntity.class)))
                .thenAnswer(inv -> {
                    LimitOrderEntity entity = inv.getArgument(0);
                    if (entity.getId() == null) {
                        setField(entity, "id", ids.getAndIncrement());
                    }
                    if (entity.getCreatedAt() == null) {
                        setField(entity, "createdAt", Instant.now());
                    }
                    setField(entity, "updatedAt", Instant.now());
                    return entity;
                });

        when(orderRepo.findAllByInstrumentAndStatusIn(eq("AAPL"), any()))
                .thenReturn(List.of(restingSell));

        when(orderRepo.findById(10L)).thenReturn(Optional.of(restingSell));

        var response = service.place(
                1L,
                new PlaceOrderRequest("AAPL", Side.BUY, OrderType.LIMIT, new BigDecimal("101.00"), 3)
        );

        assertEquals("FILLED", response.status());
        verify(fillRepo, times(1)).save(any(TradeFillEntity.class));
        verify(orderRepo, atLeastOnce()).save(any(LimitOrderEntity.class));
        verify(publisher, atLeastOnce()).orderChanged(any());
        verify(publisher, times(1)).tradeExecuted(any());
        verify(orderAuditService, atLeastOnce()).recordPlaced(any());
        verify(orderAuditService, atLeastOnce()).recordFill(any(), anyLong());
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}