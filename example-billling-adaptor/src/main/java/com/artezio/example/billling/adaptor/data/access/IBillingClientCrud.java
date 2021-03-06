/*
 */
package com.artezio.example.billling.adaptor.data.access;

import com.artezio.example.billling.adaptor.data.entities.BillingClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data access operations for billing client records.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Repository
public interface IBillingClientCrud extends JpaRepository<BillingClient, Long> {

}
