package com.aghaz.orderbook.trading.api;

import com.aghaz.orderbook.trading.domain.OrderType;
import com.aghaz.orderbook.trading.domain.Side;
import com.aghaz.orderbook.trading.infra.entity.LimitOrderEntity;
import com.aghaz.orderbook.trading.infra.repo.LimitOrderRepo;
import com.aghaz.orderbook.trading.infra.repo.OrderAuditLogRepo;
import com.aghaz.orderbook.trading.infra.repo.TradeFillRepo;
import com.aghaz.orderbook.trading.security.JwtTokenService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TradingApiFlowIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    LimitOrderRepo limitOrderRepo;

    @Autowired
    TradeFillRepo tradeFillRepo;

    @Autowired
    OrderAuditLogRepo orderAuditLogRepo;

    @MockitoBean
    JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        orderAuditLogRepo.deleteAll();
        tradeFillRepo.deleteAll();
        limitOrderRepo.deleteAll();

        when(jwtTokenService.readTraderId("trader-token")).thenReturn(1L);
        when(jwtTokenService.readRoles("trader-token")).thenReturn(List.of("ROLE_TRADER"));

        when(jwtTokenService.readTraderId("admin-token")).thenReturn(99L);
        when(jwtTokenService.readRoles("admin-token")).thenReturn(List.of("ROLE_ADMIN"));
    }

    @Test
    void traderWorkflow_shouldPlaceOrderViewOrdersViewEventsAndViewTradeHistory() throws Exception {
        limitOrderRepo.save(
                LimitOrderEntity.place(
                        2L,
                        "AAPL",
                        Side.SELL,
                        OrderType.LIMIT,
                        new BigDecimal("100.00"),
                        5
                )
        );

        String placeResponse = mvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer trader-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "instrument":"AAPL",
                                  "side":"BUY",
                                  "orderType":"LIMIT",
                                  "limitPrice":101.00,
                                  "quantity":3
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instrument").value("AAPL"))
                .andExpect(jsonPath("$.status").value("FILLED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode placedOrder = objectMapper.readTree(placeResponse);
        long orderId = placedOrder.get("id").asLong();

        mvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer trader-token")
                        .param("instrument", "AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(orderId))
                .andExpect(jsonPath("$.content[0].instrument").value("AAPL"));

        mvc.perform(get("/api/orders/{id}/events", orderId)
                        .header("Authorization", "Bearer trader-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").exists());

        mvc.perform(get("/api/trades/mine")
                        .header("Authorization", "Bearer trader-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].instrument").value("AAPL"))
                .andExpect(jsonPath("$.content[0].mySide").value("BUY"));
    }

    @Test
    void adminAccess_shouldBeForbiddenForTrader_andAllowedForAdmin() throws Exception {
        mvc.perform(get("/api/admin/summary")
                        .header("Authorization", "Bearer trader-token"))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/admin/summary")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk());
    }

    @Test
    void invalidOrExpiredToken_shouldReturn401() throws Exception {
        when(jwtTokenService.readTraderId("expired-token")).thenThrow(new RuntimeException("Expired token"));

        mvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer expired-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "instrument":"AAPL",
                                  "side":"BUY",
                                  "orderType":"LIMIT",
                                  "limitPrice":100.00,
                                  "quantity":1
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }
}