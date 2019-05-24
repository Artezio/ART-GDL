/*
 */
package com.artezio.example.billling.adaptor.services;

import com.artezio.example.billling.adaptor.camel.BillingAdaptorRoute;
import com.artezio.example.billling.adaptor.data.access.IPaymentRequestCrud;
import com.artezio.example.billling.adaptor.data.entities.PaymentRequest;
import com.artezio.example.billling.adaptor.data.types.PaymentState;
import com.artezio.recovery.server.context.RecoveryRoutes;
import com.artezio.recovery.server.data.access.IRecoveryOrderCrud;
import com.artezio.recovery.server.data.messages.RecoveryRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
     * Current Apache Camel context.
     */
    @Autowired
    private CamelContext camel;
    /**
     * Recovery records data access object.
     */
    @Autowired
    private IRecoveryOrderCrud daoRecovery;
    /**
     * Payment requests data access object.
     */
    @Autowired
    private IPaymentRequestCrud daoPayments;
    /**
     * Recovery request income route producer.
     */
    @Produce(uri = RecoveryRoutes.INCOME_URL)
    private ProducerTemplate producer;

    /**
     * Stop all current processes.
     *
     * @throws Exception
     */
    public void stopAll() throws Exception {
        camel.stop();
        daoRecovery.deleteAll();
        daoPayments.cancelProcessing();
        camel.start();
    }

    /**
     * Check is processing available.
     *
     * @return True if processing available.
     */
    public boolean isProcessing() {
        return camel.getStatus().isStarted();
    }

    /**
     * Start all payment request processes.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @SuppressWarnings("ThrowableResultIgnored")
    public void startAll() {
        if (!isProcessing()) {
            return;
        }
        Page<PaymentRequest> page = daoPayments.getNew(PageRequest.of(0, PAGE_SIZE));
        while (page.hasContent()) {
            for (PaymentRequest payment : page) {
                try {
                    startRequest(payment);
                } catch (CamelExecutionException ex) {
                    Throwable t = (ex.getCause() == null) ? ex : ex.getCause();
                    String error = t.getClass().getSimpleName()
                            + ": "
                            + t.getMessage();
                    log.error(error);
                    payment.setPaymentState(PaymentState.SYSTEM_ERROR);
                    payment.setDescription(error);
                }
            }
            page = daoPayments.getNew(PageRequest.of(0, PAGE_SIZE));
        }
    }

    /**
     * Start payment request process.
     *
     * @param payment Payment request records.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void startRequest(PaymentRequest payment) {
        if (payment == null) {
            return;
        }
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
        request.setQueue(payment.getQueue());
        request.setQueueParent(payment.getQueueParent());
        producer.sendBody(request);
    }
}
