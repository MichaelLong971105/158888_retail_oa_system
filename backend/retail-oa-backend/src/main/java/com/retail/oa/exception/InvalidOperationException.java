package com.retail.oa.exception;

/**
 * Thrown when a requested state change violates business rules.
 */
public class InvalidOperationException extends RuntimeException {
    public InvalidOperationException(String message) {
        super(message);
    }
}
