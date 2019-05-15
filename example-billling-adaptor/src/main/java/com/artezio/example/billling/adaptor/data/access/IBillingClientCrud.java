/*
 */
package com.artezio.example.billling.adaptor.data.access;

import com.artezio.example.billling.adaptor.data.entities.BillingClient;
import org.springframework.data.repository.CrudRepository;

/**
 * Data access operations for billing client records.
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
public interface IBillingClientCrud extends CrudRepository<BillingClient, Long> {
    
}
