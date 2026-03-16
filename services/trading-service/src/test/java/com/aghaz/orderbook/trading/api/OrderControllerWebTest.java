package com.aghaz.orderbook.trading.api;

import com.aghaz.orderbook.shared_contracts.api.PagedResponse;
import com.aghaz.orderbook.trading.app.OrderAuditQueryService;
import com.aghaz.orderbook.trading.app.OrderCommandService;
import com.aghaz.orderbook.trading.app.OrderQueryService;
import com.aghaz.orderbook.trading.dto.OrderResponse;
import com.aghaz.orderbook.trading.security.JwtTokenService;
import com.aghaz.orderbook.trading.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerWebTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    OrderCommandService commands;

    @MockitoBean
    OrderQueryService queries;

    @MockitoBean
    OrderAuditQueryService auditQueries;

    @MockitoBean
    JwtTokenService jwt;

    @Test
    void shouldReturn401_whenNoToken() throws Exception {
        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"instrument":"AAPL","side":"BUY","orderType":"LIMIT","limitPrice":100.0,"quantity":10}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn400_whenInvalidBody_evenWithToken() throws Exception {
        when(jwt.readTraderId("valid")).thenReturn(1L);
        when(jwt.readRoles("valid")).thenReturn(List.of("ROLE_TRADER"));

        mvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"instrument":"","side":"BUY","orderType":"LIMIT","limitPrice":100.0,"quantity":0}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void myOrders_shouldReturn200_whenAuthenticated_andAllowFiltersAndPagination() throws Exception {
        when(jwt.readTraderId("valid")).thenReturn(1L);
        when(jwt.readRoles("valid")).thenReturn(List.of("ROLE_TRADER"));
        when(queries.myOrders(1L, "AAPL", "NEW", "BUY", "LIMIT", 0, 10, "createdAt", "desc"))
                .thenReturn(new PagedResponse<OrderResponse>(List.of(), 0, 10, 0, 0, true, true, "createdAt", "desc"));

        mvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer valid")
                        .param("instrument", "AAPL")
                        .param("status", "NEW")
                        .param("side", "BUY")
                        .param("orderType", "LIMIT")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk());
    }
}