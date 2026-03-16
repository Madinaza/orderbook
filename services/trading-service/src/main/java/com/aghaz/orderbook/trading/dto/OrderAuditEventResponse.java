package com.aghaz.orderbook.trading.dto;

import java.time.Instant;

public record OrderAuditEventResponse(
        Long id,
        String eventType,
        String message,
        Instant createdAt
) {}