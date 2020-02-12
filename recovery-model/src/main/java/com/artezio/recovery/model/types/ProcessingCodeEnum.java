/*
 */
package com.artezio.recovery.model.types;

/**
 * Recovery request short processing code enumeration.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
public enum ProcessingCodeEnum {

    /**
     * Recovery order is successfully processed but client has not returned an
     * response code.
     */
    SUCCESS_DELIVERY,
    /**
     * Recovery client has returned the success code.
     */
    SUCCESS_CLIENT,
    /**
     * Recovery order is new one.
     */
    NEW,
    /**
     * Recovery order is reviewed by the callback execution processor.
     */
    REVIEWED,
    /**
     * Recovery order is expired by number of tries.
     */
    EXPIRED_BY_NUMBER,
    /**
     * Recovery order is expired by delivery period.
     */
    EXPIRED_BY_DATE,
    /**
     * Recovery order processing is interrupted by an exception.
     */
    ERROR_DELIVERY,
    /**
     * Recovery client has returned a error.
     */
    ERROR_CLIENT,
    /**
     * Recovery client has returned a business error.
     */
    ERROR_BUSINESS,
    /**
     * Recovery processing has found wrong response object.
     */
    FATAL_WRONG_RESPONSE,
    /**
     * Recovery client has returned an fatal error.
     */
    FATAL_CLIENT,
    /**
     * Recovery client has returned an fatal business error.
     */
    FATAL_BUSINESS
    
}
