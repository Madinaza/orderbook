package com.aghaz.orderbook.trading.bdd;

import com.aghaz.orderbook.trading.app.OrderAuditService;
import com.aghaz.orderbook.trading.app.OrderCommandService;
import com.aghaz.orderbook.trading.domain.OrderType;
import com.aghaz.orderbook.trading.domain.Side;
import com.aghaz.orderbook.trading.dto.OrderResponse;
import com.aghaz.orderbook.trading.dto.PlaceOrderRequest;
import com.aghaz.orderbook.trading.infra.entity.LimitOrderEntity;
import com.aghaz.orderbook.trading.infra.repo.LimitOrderRepo;
import com.aghaz.orderbook.trading.infra.repo.TradeFillRepo;
import com.aghaz.orderbook.trading.messaging.TradingEventPublisher;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PlaceOrderSteps {

    private final LimitOrderRepo orderRepo = mock(LimitOrderRepo.class);
    private final TradeFillRepo tradeFillRepo = mock(TradeFillRepo.class);
    private final TradingEventPublisher publisher = mock(TradingEventPublisher.class);
    private final OrderAuditService orderAuditService = mock(OrderAuditService.class);

    private final OrderCommandService service =
            new OrderCommandService(orderRepo, tradeFillRepo, publisher, orderAuditService);

    private final AtomicLong ids = new AtomicLong(1);

    private long traderId;
    private OrderResponse response;

    @Given("trader id {int} is authenticated")
    public void trader_id_is_authenticated(Integer traderId) {
        this.traderId = traderId.longValue();

        reset(orderRepo, tradeFillRepo, publisher, orderAuditService);

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
    }

    @When("the trader places a BUY LIMIT order for AAPL at {double} with quantity {int}")
    public void the_trader_places_a_buy_limit_order_for_aapl_at_with_quantity(Double price, Integer quantity) {
        PlaceOrderRequest request = new PlaceOrderRequest(
                "AAPL",
                Side.BUY,
                OrderType.LIMIT,
                BigDecimal.valueOf(price),
                quantity.longValue()
        );

        response = service.place(traderId, request);
    }

    @Then("the order should be accepted")
    public void the_order_should_be_accepted() {
        assertNotNull(response);
        assertNotNull(response.id());
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to set field: " + fieldName, ex);
        }
    }
}