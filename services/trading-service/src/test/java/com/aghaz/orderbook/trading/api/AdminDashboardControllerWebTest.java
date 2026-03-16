package com.aghaz.orderbook.trading.api;

import com.aghaz.orderbook.trading.app.AdminDashboardQueryService;
import com.aghaz.orderbook.trading.dto.admin.AdminDashboardResponse;
import com.aghaz.orderbook.trading.dto.admin.InstrumentSummaryRow;
import com.aghaz.orderbook.trading.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminDashboardControllerWebTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    AdminDashboardQueryService adminDashboardQueryService;

    @MockitoBean
    JwtTokenService jwtTokenService;

    @Test
    void adminSummary_shouldReturn403_forTraderRole() throws Exception {
        when(jwtTokenService.readTraderId("trader-token")).thenReturn(1L);
        when(jwtTokenService.readRoles("trader-token")).thenReturn(List.of("ROLE_TRADER"));

        mvc.perform(get("/api/admin/summary")
                        .header("Authorization", "Bearer trader-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminSummary_shouldReturn200_forAdminRole() throws Exception {
        when(jwtTokenService.readTraderId("admin-token")).thenReturn(99L);
        when(jwtTokenService.readRoles("admin-token")).thenReturn(List.of("ROLE_ADMIN"));

        when(adminDashboardQueryService.summary()).thenReturn(
                new AdminDashboardResponse(
                        0L,                         // totalOrders
                        0L,                         // openOrders
                        0L,                         // filledOrders
                        0L,                         // cancelledOrders
                        0L,                         // totalTrades
                        0L,                         // activeInstruments
                        BigDecimal.ZERO,            // buySellRatio
                        BigDecimal.ZERO,            // filledRatePercent
                        BigDecimal.ZERO,            // cancelRatePercent
                        List.of(
                                new InstrumentSummaryRow("AAPL", 0L, 0L, 0L)
                        ),
                        List.of(
                                new InstrumentSummaryRow("AAPL", 0L, 0L, 0L)
                        ),
                        List.of(
                                new InstrumentSummaryRow("AAPL", 0L, 0L, 0L)
                        )
                )
        );

        mvc.perform(get("/api/admin/summary")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk());
    }
}