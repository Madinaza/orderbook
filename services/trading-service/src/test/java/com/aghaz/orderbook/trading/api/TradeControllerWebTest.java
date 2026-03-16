package com.aghaz.orderbook.trading.api;

import com.aghaz.orderbook.shared_contracts.api.PagedResponse;
import com.aghaz.orderbook.trading.app.TradeQueryService;
import com.aghaz.orderbook.trading.dto.TradeResponse;
import com.aghaz.orderbook.trading.security.JwtTokenService;
import com.aghaz.orderbook.trading.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TradeController.class)
@Import(SecurityConfig.class)
class TradeControllerWebTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    TradeQueryService tradeQueryService;

    @MockitoBean
    JwtTokenService jwtTokenService;

    @Test
    void myTrades_shouldReturn401_whenNoToken() throws Exception {
        mvc.perform(get("/api/trades/mine"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void myTrades_shouldReturn200_whenTokenPresent() throws Exception {
        when(jwtTokenService.readTraderId("valid")).thenReturn(1L);
        when(jwtTokenService.readRoles("valid")).thenReturn(List.of("ROLE_TRADER"));
        when(tradeQueryService.myTrades(eq(1L), any(), any(), any(), any(), eq(0), eq(10), eq("executedAt"), eq("desc")))
                .thenReturn(new PagedResponse<TradeResponse>(List.of(), 0, 10, 0, 0, true, true, "executedAt", "desc"));

        mvc.perform(get("/api/trades/mine")
                        .header("Authorization", "Bearer valid"))
                .andExpect(status().isOk());
    }
}