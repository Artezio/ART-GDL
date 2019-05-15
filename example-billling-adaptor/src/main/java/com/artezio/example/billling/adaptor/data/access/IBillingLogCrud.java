/*
 */
package com.artezio.example.billling.adaptor.data.access;

import com.artezio.example.billling.adaptor.data.entities.BillingLog;
import org.springframework.data.repository.CrudRepository;

/**
 * Data access operations for billing log records.
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
public interface IBillingLogCrud extends CrudRepository<BillingLog, Long> {
    
}
