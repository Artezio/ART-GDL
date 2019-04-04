/*
 */
package com.artezio.recovery.server.data.types;

/**
 * Recovery processing exception.
 * 
 * @see Exception
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
public class RecoveryException extends Exception {

    public RecoveryException() {
    }

    public RecoveryException(String message) {
        super(message);
    }

    public RecoveryException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecoveryException(Throwable cause) {
        super(cause);
    }

    public RecoveryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
