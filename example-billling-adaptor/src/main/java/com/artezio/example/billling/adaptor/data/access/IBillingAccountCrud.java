/*
 */
package com.artezio.example.billling.adaptor.data.access;

import com.artezio.example.billling.adaptor.data.entities.BillingAccount;
import com.artezio.example.billling.adaptor.data.entities.BillingClient;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Data access operations for billing account objects.
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Repository
public interface IBillingAccountCrud extends CrudRepository<BillingAccount, Long> {
    
    /**
     * Find billing client account.
     * 
     * @param client Billing client.
     * @return Billing client account.
     */
    @Query("SELECT a FROM BillingAccount a WHERE a.client = :client")
    BillingAccount findClientAccount(@Param("client") BillingClient client);
            
}
