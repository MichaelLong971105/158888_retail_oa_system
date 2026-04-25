package com.retail.oa.exception;

/**
 * Thrown when a supplier name conflicts with an existing record.
 */
public class DuplicateSupplierException extends RuntimeException {
    public DuplicateSupplierException(String message) {
        super(message);
    }
}
