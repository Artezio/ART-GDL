/*
 */
package com.artezio.example.billling.adaptor.data.access;

import com.artezio.example.billling.adaptor.data.entities.BillingAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data access operations for billing account objects.
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Repository
public interface IBillingAccountCrud extends JpaRepository<BillingAccount, Long> {
            
}
