package com.aghaz.orderbook.trading.api;

import com.aghaz.orderbook.trading.app.AdminDashboardQueryService;
import com.aghaz.orderbook.trading.app.AdminRoutingService;
import com.aghaz.orderbook.trading.dto.admin.AdminDashboardResponse;
import com.aghaz.orderbook.trading.dto.admin.InstrumentSummaryRow;
import com.aghaz.orderbook.trading.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import com.aghaz.orderbook.trading.security.SecurityConfig;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminDashboardController.class)
@Import(SecurityConfig.class)
class AdminDashboardControllerContractTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    AdminDashboardQueryService adminDashboardQueryService;

    @MockitoBean
    AdminRoutingService adminRoutingService;

    @MockitoBean
    JwtTokenService jwtTokenService;

    @Test
    void summary_shouldReturnExpandedContract() throws Exception {
        when(jwtTokenService.readTraderId("admin-token")).thenReturn(99L);
        when(jwtTokenService.readRoles("admin-token")).thenReturn(List.of("ROLE_ADMIN"));

        AdminDashboardResponse response = new AdminDashboardResponse(
                100,
                20,
                60,
                10,
                50,
                8,
                new BigDecimal("1.20"),
                new BigDecimal("60.00"),
                new BigDecimal("10.00"),
                List.of(new InstrumentSummaryRow("AAPL", 20, 10, 120)),
                List.of(new InstrumentSummaryRow("AAPL", 20, 10, 120)),
                List.of(new InstrumentSummaryRow("MSFT", 15, 20, 90))
        );

        when(adminDashboardQueryService.summary()).thenReturn(response);

        mvc.perform(get("/api/admin/summary")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(100))
                .andExpect(jsonPath("$.buySellRatio").value(1.20))
                .andExpect(jsonPath("$.filledRatePercent").value(60.00))
                .andExpect(jsonPath("$.cancelRatePercent").value(10.00))
                .andExpect(jsonPath("$.instruments[0].instrument").value("AAPL"))
                .andExpect(jsonPath("$.instruments[0].openQuantity").value(120))
                .andExpect(jsonPath("$.top5ActiveInstruments[0].instrument").value("AAPL"))
                .andExpect(jsonPath("$.top5TradedInstruments[0].instrument").value("MSFT"));
    }
}