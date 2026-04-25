package com.retail.oa.exception;

/**
 * Thrown when a resource violates a uniqueness rule.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
