/*
 */
package com.artezio.example.billling.adaptor.data.access;

import com.artezio.example.billling.adaptor.data.entities.BillingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data access operations for billing log records.
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Repository
public interface IBillingLogCrud extends JpaRepository<BillingLog, Long> {
    
}
