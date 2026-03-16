package com.aghaz.orderbook.trading.app;

import com.aghaz.orderbook.trading.dto.admin.SystemStatusResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SystemStatusService {

    @Value("${app.kafka.enabled:false}")
    private boolean kafkaEnabled;

    public SystemStatusResponse getStatus() {
        return new SystemStatusResponse(
                new SystemStatusResponse.ServiceStatus("gateway", "UP"),
                new SystemStatusResponse.ServiceStatus("auth-service", "UP"),
                new SystemStatusResponse.ServiceStatus("trading-service", "UP"),
                new SystemStatusResponse.ServiceStatus("market-data-service", "UP"),
                "RECONNECTING",
                kafkaEnabled ? "ENABLED" : "DISABLED"
        );
    }
}