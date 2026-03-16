package com.aghaz.orderbook.trading.dto.admin;

public record SystemStatusResponse(
        ServiceStatus gateway,
        ServiceStatus authService,
        ServiceStatus tradingService,
        ServiceStatus marketDataService,
        String websocket,
        String kafkaMode
) {
    public record ServiceStatus(
            String name,
            String status
    ) {}
}