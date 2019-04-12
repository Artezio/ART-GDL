/*
 */
package com.artezio.recovery.server.data.types;

/**
 * Recovery client result type enumeration.
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
public enum ClientResultEnum {
    
    /**
     * Recovery order successfully processed.
     */
    SUCCESS,
    /**
     * Delivery error.
     */
    SYSTEM_ERROR,
    /**
     * Business processing error.
     */
    BUSINESS_ERROR,
    /**
     * Fatal delivery error. Recovery processing should be interrupted.
     */
    SYSTEM_FATAL_ERROR,
    /**
     * Fatal business error. Recovery processing should be interrupted.
     */
    BUSINESS_FATAL_ERROR;
    
}
