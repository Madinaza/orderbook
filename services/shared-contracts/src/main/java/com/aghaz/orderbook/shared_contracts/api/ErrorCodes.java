package com.aghaz.orderbook.shared_contracts.api;

/**
 * Optional: stable error codes for frontend/clients (useful for UI messages).
 * Not required, but very "real-world".
 */
public final class ErrorCodes {
    private ErrorCodes() {}

    public static final String VALIDATION = "VALIDATION";
    public static final String AUTH = "AUTH";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String BUSINESS_RULE = "BUSINESS_RULE";
    public static final String INTERNAL = "INTERNAL";
}