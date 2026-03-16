package com.aghaz.orderbook.trading.api;

import com.aghaz.orderbook.trading.app.AdminDashboardQueryService;
import com.aghaz.orderbook.trading.app.AdminRoutingService;
import com.aghaz.orderbook.trading.dto.admin.AdminDashboardResponse;
import com.aghaz.orderbook.trading.dto.admin.BestVenueRequest;
import com.aghaz.orderbook.trading.dto.admin.BestVenueResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {

    private final AdminDashboardQueryService adminDashboardQueryService;
    private final AdminRoutingService adminRoutingService;

    public AdminDashboardController(AdminDashboardQueryService adminDashboardQueryService,
                                    AdminRoutingService adminRoutingService) {
        this.adminDashboardQueryService = adminDashboardQueryService;
        this.adminRoutingService = adminRoutingService;
    }

    @GetMapping("/summary")
    public AdminDashboardResponse summary() {
        return adminDashboardQueryService.summary();
    }

    @PostMapping("/routing/best-venue")
    public BestVenueResponse bestVenue(@Valid @RequestBody BestVenueRequest request) {
        return adminRoutingService.simulateBestVenue(request);
    }
}