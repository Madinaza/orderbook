package com.aghaz.orderbook.trading.bdd;

import com.aghaz.orderbook.trading.app.OrderCommandService;
import com.aghaz.orderbook.trading.domain.OrderType;
import com.aghaz.orderbook.trading.domain.Side;
import com.aghaz.orderbook.trading.dto.OrderResponse;
import com.aghaz.orderbook.trading.dto.PlaceOrderRequest;
import com.aghaz.orderbook.trading.dto.ReplaceOrderRequest;
import com.aghaz.orderbook.trading.infra.entity.LimitOrderEntity;
import com.aghaz.orderbook.trading.infra.entity.TradeFillEntity;
import com.aghaz.orderbook.trading.infra.repo.LimitOrderRepo;
import com.aghaz.orderbook.trading.infra.repo.OrderAuditLogRepo;
import com.aghaz.orderbook.trading.infra.repo.TradeFillRepo;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;

import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderWorkflowSteps {

    @Autowired
    private OrderCommandService orderCommandService;

    @Autowired
    private LimitOrderRepo limitOrderRepo;

    @Autowired
    private TradeFillRepo tradeFillRepo;

    @Autowired
    private OrderAuditLogRepo orderAuditLogRepo;

    private OrderResponse lastOrderResponse;

    @Before
    public void clean() {
        orderAuditLogRepo.deleteAll();
        tradeFillRepo.deleteAll();
        limitOrderRepo.deleteAll();
        lastOrderResponse = null;
    }

    @Given("a resting SELL order for {word} at {double} quantity {int}")
    public void a_resting_sell_order_for_instrument_at_price_quantity(String instrument, Double price, Integer quantity) {
        limitOrderRepo.save(
                LimitOrderEntity.place(
                        2L,
                        instrument,
                        Side.SELL,
                        OrderType.LIMIT,
                        BigDecimal.valueOf(price),
                        quantity.longValue()
                )
        );
    }

    @When("trader {int} places a BUY LIMIT order for {word} at {double} quantity {int}")
    public void trader_places_a_buy_limit_order(Integer traderId, String instrument, Double price, Integer quantity) {
        lastOrderResponse = orderCommandService.place(
                traderId.longValue(),
                new PlaceOrderRequest(
                        instrument,
                        Side.BUY,
                        OrderType.LIMIT,
                        BigDecimal.valueOf(price),
                        quantity.longValue()
                )
        );
    }

    @When("trader {int} places a BUY MARKET order for {word} quantity {int}")
    public void trader_places_a_buy_market_order(Integer traderId, String instrument, Integer quantity) {
        lastOrderResponse = orderCommandService.place(
                traderId.longValue(),
                new PlaceOrderRequest(
                        instrument,
                        Side.BUY,
                        OrderType.MARKET,
                        null,
                        quantity.longValue()
                )
        );
    }

    @When("trader {int} replaces the order price to {double} and total quantity to {int}")
    public void trader_replaces_the_order(Integer traderId, Double newPrice, Integer newQuantity) {
        lastOrderResponse = orderCommandService.replace(
                traderId.longValue(),
                lastOrderResponse.id(),
                new ReplaceOrderRequest(BigDecimal.valueOf(newPrice), newQuantity.longValue())
        );
    }

    @Then("one trade should be created at {double} for quantity {int}")
    public void one_trade_should_be_created(Double price, Integer quantity) {
        List<TradeFillEntity> trades = tradeFillRepo.findTop100ByInstrumentOrderByExecutedAtDesc("AAPL");
        assertEquals(1, trades.size());
        assertEquals(0, trades.get(0).getPrice().compareTo(BigDecimal.valueOf(price)));
        assertEquals(quantity.longValue(), trades.get(0).getQuantity());
    }

    @Then("no trade should be created")
    public void no_trade_should_be_created() {
        List<TradeFillEntity> trades = tradeFillRepo.findTop100ByInstrumentOrderByExecutedAtDesc("AAPL");
        assertTrue(trades.isEmpty());
    }

    @Then("the incoming order status should be {word}")
    public void the_incoming_order_status_should_be(String expectedStatus) {
        assertNotNull(lastOrderResponse);
        assertEquals(expectedStatus, lastOrderResponse.status());
    }

    @Then("the replaced order should have open quantity {int}")
    public void the_replaced_order_should_have_open_quantity(Integer expectedOpenQty) {
        assertNotNull(lastOrderResponse);
        assertEquals(expectedOpenQty.longValue(), lastOrderResponse.openQty());
    }
}