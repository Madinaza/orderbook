package com.aghaz.orderbook.shared_contracts.exceptions;

/**
 * Thrown when the request is valid JSON, but breaks a business rule.
 * Example: canceling a FILLED order, or replacing with negative qty.
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}