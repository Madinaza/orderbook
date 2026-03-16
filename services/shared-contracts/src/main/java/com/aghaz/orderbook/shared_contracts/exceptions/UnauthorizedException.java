package com.aghaz.orderbook.shared_contracts.exceptions;

/**
 * Use when authentication/authorization fails.
 * Example: invalid credentials or missing/invalid token.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}