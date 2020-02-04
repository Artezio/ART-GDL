/*
 */
package com.artezio.recovery.server.data.types;

/**
 * Preprocessing hold code.
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
public enum HoldingCodeEnum {
    
    /**
     * No preprocessing holders.
     */
    NO_HOLDING,
    /**
     * Processing holds by date interval.
     */
    HOLDING_BY_DATE,
    /**
     * Processing holds by pause.
     */
    HOLDING_BY_PAUSE,
    /**
     * Processing holds by a queue.
     */
    HOLDING_BY_QUEUE,
    /**
     * Processing holds by a parent queue.
     */
    HOLDING_BY_PARENT_QUEUE
}
