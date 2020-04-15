/*
 */
package com.artezio.example.billling.adaptor.services;

import com.artezio.example.billling.adaptor.camel.BillingAdaptorRoute;
import com.artezio.example.billling.adaptor.data.access.IPaymentRequestCrud;
import com.artezio.example.billling.adaptor.data.access.IRecoveryClientCrud;
import com.artezio.example.billling.adaptor.data.entities.PaymentRequest;
import com.artezio.example.billling.adaptor.data.types.DeliveryMethodType;
import com.artezio.example.billling.adaptor.data.types.PaymentState;
import com.artezio.recovery.model.RecoveryRequestDTO;
import com.artezio.recovery.rest.model.RestRecoveryRequest;
import com.artezio.recovery.rest.route.RestRoute;
import com.artezio.recovery.server.context.RecoveryRoutes;
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

import java.util.concurrent.TimeUnit;

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
    private IRecoveryClientCrud daoRecovery;
    /**
     * Payment requests data access object.
     */
    @Autowired
    private IPaymentRequestCrud daoPayments;
    /**
     * Recovery request income route producer.
     */
    @Produce(uri = RecoveryRoutes.INCOME_URL)
    private ProducerTemplate directProducer;

    /**
     * Recovery request jms route producer.
     */
    @Produce(uri = "jms:p2p_recovery")
    private ProducerTemplate jmsProducer;

    /**
     * Recovery request rest route producer.
     */
    @Produce(uri = RestRoute.POST_ENDPOINT_URL + "?host=localhost:8080")
    private ProducerTemplate restProducer;

    /**
     * Recovery request rest route producer.
     */
    @Produce(uri = "kafka:test?brokers=localhost:9092&groupId=testing")
    private ProducerTemplate kafkaProducer;

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
            camel.stopRoute(RecoveryRoutes.INCOME_ID, 1, TimeUnit.MILLISECONDS);
            camel.stopRoute(RecoveryRoutes.CLEANING_ID, 1, TimeUnit.MILLISECONDS);
            camel.stopRoute(RecoveryRoutes.TIMER_ID, 1, TimeUnit.MILLISECONDS);
            camel.stopRoute(RecoveryRoutes.SEDA_ID, 5, TimeUnit.SECONDS);
            camel.stop();
            daoRecovery.deleteAll();
            daoPayments.cancelProcessing();
            camel.start();
        } catch (Exception e) {
            String error = e.getClass().getSimpleName()
                    + ": "
                    + e.getMessage();
            log.error(error);
        }

    }

    /**
     * Start all payment request processes.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    @SuppressWarnings("ThrowableResultIgnored")
    public void startAll(DeliveryMethodType deliveryMethodType) {
        if (!camel.getStatus().isStarted()) {
            return;
        }
        int pageNum = 0;
        Page<PaymentRequest> page = daoPayments.getNew(PageRequest.of(pageNum, PAGE_SIZE));
        while (page.hasContent()) {
            for (PaymentRequest payment : page) {
                try {
                    sendRequest(payment, deliveryMethodType);
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
            page = daoPayments.getNew(PageRequest.of(++pageNum, PAGE_SIZE));
        }
    }

    /**
     * Sends recovery request.
     *
     * @param payment Payment request records.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendRequest(PaymentRequest payment, DeliveryMethodType deliveryMethodType) {
        if (payment == null) {
            return;
        }
        switch (deliveryMethodType) {
            case DIRECT:
                RecoveryRequestDTO recoveryRequestDTO = prepareRequest(payment);
                directProducer.sendBody(recoveryRequestDTO.getRecoveryRequest());
                break;
            case JMS:
                RecoveryRequestDTO recoveryRequestJMS = prepareRequest(payment);
                jmsProducer.sendBody(recoveryRequestJMS);
                break;
            case REST:
                RestRecoveryRequest recoveryRequest = prepareRestRequest(payment);
                restProducer.sendBody(recoveryRequest);
                break;
        }
    }

    private RestRecoveryRequest prepareRestRequest(PaymentRequest payment) {
        RestRecoveryRequest request = new RestRecoveryRequest();
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
        return request;
    }

    private RecoveryRequestDTO prepareRequest(PaymentRequest payment){
        RecoveryRequestDTO request = new RecoveryRequestDTO();
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
        return request;
    }
}
