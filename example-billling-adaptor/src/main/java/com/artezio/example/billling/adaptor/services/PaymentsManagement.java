/*
 */
package com.artezio.example.billling.adaptor.services;

import com.artezio.example.billling.adaptor.data.access.IPaymentRequestCrud;
import com.artezio.example.billling.adaptor.data.entities.PaymentRequest;
import com.artezio.example.billling.adaptor.data.types.PaymentState;
import com.artezio.example.billling.adaptor.services.types.PaymentStateCounter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Payments management operations.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Service
public class PaymentsManagement {

    /**
     * Payment requests data access object.
     */
    @Autowired
    private IPaymentRequestCrud daoPayments;

    /**
     * Get a page of payment request records.
     *
     * @param pageNumber Data page number.
     * @param pageSize Data page size.
     * @return Data page of clients records.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public List<PaymentRequest> getPaymentsPage(int pageNumber, int pageSize) {
        List<PaymentRequest> payments = new ArrayList<>();
        Page<PaymentRequest> page = daoPayments.getPage(PageRequest.of(pageNumber, pageSize));
        payments.addAll(page.getContent());
        return payments;
    }

    /**
     * Create or update payment request record to the DB.
     *
     * @param payment Payment request record object.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void save(PaymentRequest payment) {
        if (payment == null) {
            return;
        }
        daoPayments.save(payment);
    }

    /**
     * Remove payment request record from the DB.
     *
     * @param paymentId Payment request record ID.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void remove(Long paymentId) {
        if (paymentId == null) {
            return;
        }
        daoPayments.deleteById(paymentId);
    }
    
    /**
     * Remove all payment request record from the DB.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void removeAll() {
        daoPayments.deleteAll();
    }
    
    /**
     * Handler of payment state counts.
     * 
     * @return 
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public PaymentStateCounter countStates() {
        PaymentStateCounter counter = new PaymentStateCounter();
        counter.setRegistered(daoPayments.countByState(PaymentState.REGISTERED));
        counter.setSystemError(daoPayments.countByState(PaymentState.SYSTEM_ERROR));
        counter.setProcessing(daoPayments.countByState(PaymentState.PROCESSING));
        counter.setCanceled(daoPayments.countByState(PaymentState.CANCELED));
        counter.setSuccess(daoPayments.countByState(PaymentState.SUCCESS));
        counter.setExpired(daoPayments.countByState(PaymentState.EXPIRED));
        return counter;
    }

}
