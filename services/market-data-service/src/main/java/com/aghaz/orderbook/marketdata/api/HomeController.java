package com.aghaz.orderbook.marketdata.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "service", "market-data-service",
                "status", "UP",
                "endpoints", new String[]{
                        "/api/orderbook/{instrument}",
                        "/api/trades/{instrument}",
                        "/ws"
                }
        );
    }
}