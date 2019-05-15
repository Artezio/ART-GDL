/*
 */
package com.artezio.example.billling.adaptor.data.access;

import com.artezio.example.billling.adaptor.data.entities.PaymentRequest;
import org.springframework.data.repository.CrudRepository;

/**
 * Data access operations for payment request objects.
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
public interface IPaymentRequestCrud extends CrudRepository<PaymentRequest, Long> {
    
}
