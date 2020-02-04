/*
 */
package com.artezio.recovery.server.data.exception;

/**
 * Recovery processing exception.
 * 
 * @see Exception
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
public class RecoveryException extends Exception {

    /**
     * Recovery processing exception.
     */
    public RecoveryException() {
    }

    /**
     * Recovery processing exception.
     * 
     * @param message Exception describing message
     */
    public RecoveryException(String message) {
        super(message);
    }

    /**
     * Recovery processing exception.
     * 
     * @param message Exception describing message
     * @param cause @see Throwable
     */
    public RecoveryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Recovery processing exception.
     * 
     * @param cause @see Throwable
     */
    public RecoveryException(Throwable cause) {
        super(cause);
    }

    /**
     * Recovery processing exception.
     * 
     * @param message Exception describing message
     * @param cause @see Throwable
     * @param enableSuppression @see Exception
     * @param writableStackTrace @see Exception
     */
    public RecoveryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
