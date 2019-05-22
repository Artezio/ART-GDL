/*
 */
package com.artezio.example.billling.adaptor.data.access;

import com.artezio.example.billling.adaptor.data.entities.BillingClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Data access operations for billing client records.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Repository
public interface IBillingClientCrud extends CrudRepository<BillingClient, Long> {

    /**
     * Get a page of client records.
     *
     * @param pageable Data page settings.
     * @return Data page of clients records.
     */
    @Query("SELECT c FROM BillingClient c")
    Page<BillingClient> getPage(Pageable pageable);
}
