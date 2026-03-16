package com.aghaz.orderbook.trading.app;

import com.aghaz.orderbook.shared_contracts.exceptions.BusinessRuleException;
import com.aghaz.orderbook.trading.domain.OrderType;
import com.aghaz.orderbook.trading.domain.Side;
import com.aghaz.orderbook.trading.dto.PlaceOrderRequest;
import com.aghaz.orderbook.trading.infra.entity.LimitOrderEntity;
import com.aghaz.orderbook.trading.infra.repo.LimitOrderRepo;
import com.aghaz.orderbook.trading.infra.repo.TradeFillRepo;
import com.aghaz.orderbook.trading.messaging.TradingEventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OrderCommandServiceTest {

    private final LimitOrderRepo orderRepo = mock(LimitOrderRepo.class);
    private final TradeFillRepo tradeFillRepo = mock(TradeFillRepo.class);
    private final TradingEventPublisher publisher = mock(TradingEventPublisher.class);
    private final OrderAuditService orderAuditService = mock(OrderAuditService.class);

    private final OrderCommandService service =
            new OrderCommandService(orderRepo, tradeFillRepo, publisher, orderAuditService);

    private final AtomicLong ids = new AtomicLong(1);

    @Test
    void place_shouldNormalizeInstrument_andSaveOrder() {
        PlaceOrderRequest request = new PlaceOrderRequest(
                "aapl",
                Side.BUY,
                OrderType.LIMIT,
                BigDecimal.valueOf(100.50),
                10
        );

        when(orderRepo.save(any(LimitOrderEntity.class)))
                .thenAnswer(invocation -> {
                    LimitOrderEntity entity = invocation.getArgument(0);
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
                .thenReturn(List.of());

        var response = service.place(1L, request);

        ArgumentCaptor<LimitOrderEntity> captor = ArgumentCaptor.forClass(LimitOrderEntity.class);
        verify(orderRepo, atLeastOnce()).save(captor.capture());

        LimitOrderEntity firstSaved = captor.getAllValues().get(0);

        assertEquals("AAPL", firstSaved.getInstrument());
        assertEquals("AAPL", response.instrument());
        assertEquals("NEW", response.status());

        verifyNoInteractions(tradeFillRepo);
        verify(publisher, atLeastOnce()).orderChanged(any());
        verify(orderAuditService, atLeastOnce()).recordPlaced(any());
    }

    @Test
    void place_shouldRejectLimitOrderWithoutPrice() {
        PlaceOrderRequest request = new PlaceOrderRequest(
                "AAPL",
                Side.BUY,
                OrderType.LIMIT,
                null,
                10
        );

        assertThrows(BusinessRuleException.class, () -> service.place(1L, request));

        verifyNoInteractions(orderRepo);
        verifyNoInteractions(tradeFillRepo);
        verifyNoInteractions(publisher);
        verifyNoInteractions(orderAuditService);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}