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
     * Account is opened to enroll new operations.
     */
    OPENED,
    /**
     * Account is locked.
     */
    LOCKED
    
}
