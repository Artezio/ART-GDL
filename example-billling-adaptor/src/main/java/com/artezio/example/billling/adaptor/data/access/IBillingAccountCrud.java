/*
 */
package com.artezio.example.billling.adaptor.data.access;

import com.artezio.example.billling.adaptor.data.entities.BillingAccount;
import org.springframework.data.repository.CrudRepository;

/**
 * Data access operations for billing account objects.
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
public interface IBillingAccountCrud extends CrudRepository<BillingAccount, Long> {
    
}
