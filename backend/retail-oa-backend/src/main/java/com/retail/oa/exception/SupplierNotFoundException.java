package com.retail.oa.exception;

/**
 * Thrown when a supplier cannot be found.
 */
public class SupplierNotFoundException extends RuntimeException {
    public SupplierNotFoundException(String message) {
        super(message);
    }
}
