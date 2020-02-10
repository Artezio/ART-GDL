/*
 */
package com.artezio.example.billling.adaptor.services;

import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.artezio.example.billling.adaptor.camel.BillingAdaptorRoute;
import com.artezio.example.billling.adaptor.data.access.IPaymentRequestCrud;
import com.artezio.example.billling.adaptor.data.access.IRecoveryClientCrud;
import com.artezio.example.billling.adaptor.data.entities.PaymentRequest;
import com.artezio.example.billling.adaptor.data.types.PaymentState;
import com.artezio.recovery.client.RecoveryRequestService;
import com.artezio.recovery.server.data.exception.RecoveryException;
import com.artezio.recovery.server.data.model.RecoveryRequest;
import com.artezio.recovery.server.data.types.DeliveryMethodType;
import com.artezio.recovery.server.routes.RecoveryRoute;

import lombok.extern.slf4j.Slf4j;

/**
 * Batch operations with payment processing.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Service
@Slf4j
public class BatchProcessing {

    /**
     * Size of data page to upload payment request records.
     */
    private static final int PAGE_SIZE = 1000;

    /**
     * Recovery request route producer handler.
     */
    @Autowired
    RecoveryRequestService service;

    /**
     * Current Apache Camel context.
     */
    @Autowired
    private CamelContext camel;
    /**
     * Recovery records data access object.
     */
    @Autowired
    private IRecoveryClientCrud daoRecovery;
    /**
     * Payment requests data access object.
     */
    @Autowired
    private IPaymentRequestCrud daoPayments;

    /**
     * Count all processing recovery orders.
     * 
     * @return Number of all processing recovery orders.
     */
    public long countProcessingOrders() {
        return daoRecovery.countProcessingOrders();
    }

    /**
     * Count paused processing recovery orders.
     * 
     * @return Number of paused processing recovery orders.
     */
    public long countPausedOrders() {
        return daoRecovery.countPausedOrders();
    }
    
    /**
     * Stop all current processes.
     */
    public void stopAll() {
        try {
            camel.stopRoute(RecoveryRoute.INCOME_ID, 1, TimeUnit.MILLISECONDS);
            camel.stopRoute(RecoveryRoute.CLEANING_ID, 1, TimeUnit.MILLISECONDS);
            camel.stopRoute(RecoveryRoute.TIMER_ID, 1, TimeUnit.MILLISECONDS);
            camel.stopRoute(RecoveryRoute.SEDA_ID, 5, TimeUnit.SECONDS);
            camel.stop();
            daoRecovery.deleteAll();
            daoPayments.cancelProcessing();
            camel.start();
        } catch (Exception e) {
            String error = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error(error);
        }

    }

    /**
     * Start all payment request processes.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @SuppressWarnings("ThrowableResultIgnored")
    public void startAll(DeliveryMethodType deliveryMethodType) {
        if (camel.getStatus().isStarted()) {
//        if (service.isServerStarted()) {
            int pageNum = 0;
            Page<PaymentRequest> page = daoPayments.getNew(PageRequest.of(pageNum, PAGE_SIZE));
            while (page.hasContent()) {
                for (PaymentRequest payment : page) {
                    try {
                        startRequest(payment, deliveryMethodType);
                    } catch (CamelExecutionException | RecoveryException ex) {
                        Throwable t = (ex.getCause() == null) ? ex : ex.getCause();
                        String error = t.getClass().getSimpleName()
                                + ": "
                                + t.getMessage();
                        log.error(error);
                        payment.setPaymentState(PaymentState.SYSTEM_ERROR);
                        payment.setDescription(error);
                    }
                }
                page = daoPayments.getNew(PageRequest.of(++pageNum, PAGE_SIZE));
            }
        }
    }

    /**
     * Start payment request process.
     *
     * @param payment Payment request records.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void startRequest(PaymentRequest payment, DeliveryMethodType deliveryMethodType) throws RecoveryException {
        if (payment != null) {
            RecoveryRequest request = new RecoveryRequest();
            request.setCallbackUri(BillingAdaptorRoute.ADAPTOR_URL);
            request.setExternalId(String.valueOf(payment.getId()));
            request.setLocker((payment.getLocker() == null)
                    ? this.getClass().getSimpleName() + "-" + String.valueOf(payment.getId())
                    : payment.getLocker());
            request.setMessage(payment.getOperationType().name());
            request.setPause(payment.getPause());
            request.setProcessingFrom(payment.getProcessingFrom());
            request.setProcessingLimit(payment.getProcessingLimit());
            request.setProcessingTo(payment.getProcessingTo());
            request.setQueue(payment.getQueue() == null ? null : payment.getQueue().replace("\\s+", ""));
            request.setQueueParent(payment.getQueueParent());
            service.sendRequest(deliveryMethodType, request);
        }
    }
}
