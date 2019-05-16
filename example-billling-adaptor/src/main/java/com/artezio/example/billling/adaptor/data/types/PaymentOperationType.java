/*
 */
package com.artezio.example.billling.adaptor.data.types;

/**
 * Payment operation types.
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
public enum PaymentOperationType {
    
    /**
     * Lock an client account.
     */
    LOCK_ACCOUNT,
    /**
     * Unlock an client account.
     */
    UNLOCK_ACCOUNT,
    /**
     * Enroll a payment to the client account.
     */
    ENROLL_PAYMENT;
    
}
