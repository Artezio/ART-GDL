/*
 */
package com.artezio.example.billling.adaptor.data.types;

/**
 * Client billing account states.
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
public enum ClientAccountState {
    
    /**
     * Account created. No operations committed.
     */
    NEW,
    /**
     * Account is open to enroll new operations.
     */
    OPEN,
    /**
     * Account is locked.
     */
    LOCKED
    
}
