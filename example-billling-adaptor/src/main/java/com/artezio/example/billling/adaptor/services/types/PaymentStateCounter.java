/*
 */
package com.artezio.example.billling.adaptor.services.types;

import lombok.Data;

/**
 * Handler of payment state counts.
 * <pre>
 * @see com.artezio.example.billling.adaptor.data.types.PaymentState
 *  registered - counter for REGISTERED payment state.
 *  systemError - counter for SYSTEM_ERROR payment state.
 *  processing - counter for PROCESSING payment state.
 *  canceled - counter for CANCELED payment state.
 *  success - counter for SUCCESS payment state.
 *  expired - counter for EXPIRED payment state.
 *  all - count all records.
 * </pre>
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Data
public class PaymentStateCounter {
    
    private long registered;
    private long systemError;
    private long processing;
    private long canceled;
    private long success;
    private long expired;
    private long all;
    
}
