package com.aghaz.orderbook.shared_contracts.exceptions;

/**
 * Use when a requested resource does not exist.
 * Example: /orders/99999 where 99999 isn't present.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}