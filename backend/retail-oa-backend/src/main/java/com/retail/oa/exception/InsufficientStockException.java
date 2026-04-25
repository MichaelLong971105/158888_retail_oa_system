package com.retail.oa.exception;

/**
 * Thrown when a stock operation cannot be completed safely.
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
