/*
 */
package com.artezio.recovery.server.data.types;

/**
 * Recovery request processing status enumeration.
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
public enum RecoveryStatusEnum {
    
    /**
     * Recovery request in processing.
     */
    PROCESSING,
    /**
     * Recovery request is successfully processed.
     */
    SUCCESS,
     /**
     * Recovery request is completed with error.
     */
    ERROR
    
}
