/*
 */
package com.artezio.example.billling.adaptor.data.types;

/**
 * Example payment state.
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
public enum PaymentState {
    
    /**
     * Payment is registered.
     */
    REGISTERED,
    /**
     * Example execution error.
     */
    SYSTEM_ERROR,
    /**
     * Payment is currently processing.
     */
    PROCESSING,
    /**
     * Payment process canceled by user.
     */
    CANCELED,
    /**
     * Payment is successfully processed.
     */
    SUCCESS,
    /**
     * Payment is expired.
     */
    EXPIRED;
}
