package com.aghaz.orderbook.shared_contracts.api;

import java.time.Instant;

/**
 * Standard API error payload across all microservices.
 * Keeps frontend + API clients consistent no matter which service throws the error.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {}