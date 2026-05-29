package com.retail.oa.entity;

/**
 * Lifecycle status for a purchase order.
 */
public enum OrderStatus {
    PENDING,
    RECEIVED,
    /**
     * Legacy value kept so older local databases can still be read.
     * API responses normalize it to RECEIVED.
     */
    COMPLETED,
    CANCELLED
}
