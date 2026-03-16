package com.aghaz.orderbook.trading.api;

import com.aghaz.orderbook.trading.app.SystemStatusService;
import com.aghaz.orderbook.trading.dto.admin.SystemStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemStatusController {

    private final SystemStatusService systemStatusService;

    public SystemStatusController(SystemStatusService systemStatusService) {
        this.systemStatusService = systemStatusService;
    }

    @GetMapping("/status")
    public SystemStatusResponse status() {
        return systemStatusService.getStatus();
    }
}