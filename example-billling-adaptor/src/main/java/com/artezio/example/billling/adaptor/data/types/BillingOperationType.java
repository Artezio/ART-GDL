/*
 */
package com.artezio.example.billling.adaptor.data.types;

/**
 * Billing operation types
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
public enum BillingOperationType {
    
    /**
     * Client account is created.
     */
    ACCOUNT_CREATED,
    /**
     * Client account is locked.
     */
    ACCOUNT_LOCKED,
    /**
     * Payment committed to an client account.
     */
    PAYMENT_COMMITTED,
    /**
     * Payment commit refused.
     */
    PAYMENT_REFUSED
    
}
