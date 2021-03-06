/*
 */
package com.artezio.example.billling.adaptor.data.access;

import com.artezio.example.billling.adaptor.data.entities.PaymentRequest;
import com.artezio.example.billling.adaptor.data.types.PaymentState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Data access operations for payment request objects.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Repository
public interface IPaymentRequestCrud extends JpaRepository<PaymentRequest, Long> {

    /**
     * Get new payment requests for billing processing.
     *
     * @param pageable Data page settings.
     * @return Data page of payment requests records.
     */
    @Query("SELECT r FROM PaymentRequest r WHERE r.paymentState = com.artezio.example.billling.adaptor.data.types.PaymentState.REGISTERED")
    Page<PaymentRequest> getNew(Pageable pageable);

    /**
     * Set canceled state to current billing processing requests.
     */
    @Modifying
    @Query("UPDATE PaymentRequest r SET"
            + " r.paymentState = com.artezio.example.billling.adaptor.data.types.PaymentState.CANCELED "
            + "WHERE"
            + " r.paymentState = com.artezio.example.billling.adaptor.data.types.PaymentState.PROCESSING")
    void cancelProcessing();

    /**
     * Count payment requests by state.
     *
     * @param paymentState Payment request state.
     * @return Number of payment requests with the state.
     */
    @Query("SELECT COUNT(r) FROM PaymentRequest r WHERE r.paymentState = :paymentState")
    long countByState(@Param("paymentState") PaymentState paymentState);
    
    /**
     * Count success number of processing tries.
     *
     * @return Number of resting processing tries.
     */
    @Query("SELECT SUM(r.successCount) FROM PaymentRequest r WHERE"
            + " r.paymentState = com.artezio.example.billling.adaptor.data.types.PaymentState.PROCESSING"
            + " OR r.paymentState = com.artezio.example.billling.adaptor.data.types.PaymentState.REGISTERED")
    long countSuccessTries();

}
