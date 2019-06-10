/*
 */
package com.artezio.example.billling.adaptor.camel;

import com.artezio.example.billling.adaptor.data.access.IBillingAccountCrud;
import com.artezio.example.billling.adaptor.data.access.IBillingLogCrud;
import com.artezio.example.billling.adaptor.data.access.IPaymentRequestCrud;
import com.artezio.example.billling.adaptor.data.entities.BillingAccount;
import com.artezio.example.billling.adaptor.data.entities.BillingLog;
import com.artezio.example.billling.adaptor.data.entities.PaymentRequest;
import com.artezio.example.billling.adaptor.data.types.BillingOperationType;
import com.artezio.example.billling.adaptor.data.types.ClientAccountState;
import com.artezio.example.billling.adaptor.data.types.PaymentState;
import com.artezio.recovery.server.data.messages.ClientResponse;
import com.artezio.recovery.server.data.messages.RecoveryOrder;
import com.artezio.recovery.server.data.types.ClientResultEnum;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Example billing adaptor Apache Camel Route.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Component
@Slf4j
public class BillingAdaptorRoute extends SpringRouteBuilder {

    /**
     * Billing adaptor route ID.
     */
    public static final String ADAPTOR_ID = "ExampleBillingAdaptor";
    /**
     * Billing adaptor route URL.
     */
    public static final String ADAPTOR_URL = "direct://" + ADAPTOR_ID;
    /**
     * Timeout in milliseconds to emulate long term remote execution.
     */
    private static final int PRODUCER_TIMEOUT = 5_000;
    /**
     * Billing operations data access object.
     */
    @Autowired
    private IBillingLogCrud daoLog;
    /**
     * Payment requests data access object.
     */
    @Autowired
    private IPaymentRequestCrud daoPayments;
    /**
     * Billing accounts data access object.
     */
    @Autowired
    private IBillingAccountCrud daoAccounts;

    /**
     * Billing adaptor Apache Camel Route definition.
     *
     * @throws Exception
     */
    @Override
    @SuppressWarnings("UseSpecificCatch")
    public void configure() throws Exception {
        from(ADAPTOR_URL).id(ADAPTOR_ID)
                .process(e -> {
                    log.info(e.getExchangeId()
                            + ": "
                            + Thread.currentThread().getName());
                    // Long term process emulation.
                    Thread.sleep(PRODUCER_TIMEOUT);
                    String txt = "No action commited.";
                    BillingLog msg = new BillingLog();
                    msg.setOperationType(BillingOperationType.PAYMENT_REFUSED);
                    msg.setDescription(txt);
                    ClientResponse response = new ClientResponse();
                    response.setResult(ClientResultEnum.SYSTEM_FATAL_ERROR);
                    response.setDescription(txt);
                    PaymentRequest payment = null;
                    BillingAccount account = null;
                    main:
                    try {
                        Object body = e.getIn().getBody();
                        if (!(body instanceof RecoveryOrder)) {
                            txt = "Wrong recovery message. Recieved: "
                                    + ((body == null) ? "NULL" : body.getClass().getSimpleName());
                            break main;
                        }
                        RecoveryOrder order = (RecoveryOrder) body;
                        String extId = order.getExternalId();
                        if (extId == null) {
                            txt = "Wrong recovery message. Extenal ID is NULL.";
                            break main;
                        }
                        msg.setExternalId(extId);
                        if (!extId.matches("\\d+")) {
                            txt = "Wrong extenal ID.";
                            break main;
                        }
                        Long paymentId = Long.parseLong(extId);
                        Optional<PaymentRequest> paymentRecord = daoPayments.findById(paymentId);
                        if (!paymentRecord.isPresent()) {
                            txt = "Payment record is not found.";
                            response.setResult(ClientResultEnum.BUSINESS_FATAL_ERROR);
                            break main;
                        }
                        payment = paymentRecord.get();
                        msg.setClient(payment.getClient());
                        account = payment.getClient().getAccount();
                        if (account == null) {
                            txt = "Billing account is not found.";
                            response.setResult(ClientResultEnum.BUSINESS_FATAL_ERROR);
                            payment.setPaymentState(PaymentState.SYSTEM_ERROR);
                            break main;
                        }
                        msg.setAccount(account);
                        switch (order.getCode()) {
                            case EXPIRED_BY_DATE:
                                txt = "Expired by date limitation.";
                                payment.setPaymentState(PaymentState.EXPIRED);
                                response.setResult(ClientResultEnum.SUCCESS);
                                break main;
                            case EXPIRED_BY_NUMBER:
                                txt = "Expired by number of tries limitation.";
                                payment.setPaymentState(PaymentState.EXPIRED);
                                response.setResult(ClientResultEnum.SUCCESS);
                                break main;
                        }
                        int count = order.getProcessingCount();
                        int successCount = (payment.getSuccessCount() != null)
                                ? payment.getSuccessCount()
                                : -1;
                        if (count < successCount) {
                            txt = "Hold by successful count limitation: "
                                    + count + " of " + successCount;
                            response.setResult(ClientResultEnum.BUSINESS_ERROR);
                            payment.setPaymentState(PaymentState.PROCESSING);
                            break main;
                        }
                        switch (payment.getOperationType()) {
                            case LOCK_ACCOUNT:
                                msg.setOperationType(BillingOperationType.ACCOUNT_LOCKED);
                                account.setBillingState(ClientAccountState.LOCKED);
                                txt = "Account have been locked.";
                                break;
                            case UNLOCK_ACCOUNT:
                                msg.setOperationType(BillingOperationType.ACCOUNT_UNLOCKED);
                                account.setBillingState(ClientAccountState.OPEN);
                                txt = "Account have been unlocked.";
                                break;
                            case ENROLL_PAYMENT:
                                switch (account.getBillingState()) {
                                    case OPEN:
                                    case NEW:
                                        msg.setOperationType(BillingOperationType.PAYMENT_COMMITTED);
                                        BigDecimal paymentAmount = payment.getAmount();
                                        BigDecimal accountAmount = account.getBalance();
                                        BigDecimal resultBalance = accountAmount.add(paymentAmount);
                                        account.setBalance(resultBalance);
                                        txt = String.valueOf(paymentAmount) + " enrolled.";
                                        break;
                                    case LOCKED:
                                    default:
                                        msg.setOperationType(BillingOperationType.PAYMENT_REFUSED);
                                        txt = "Account is locked.";
                                        break;
                                }
                                break;
                        }
                        payment.setPaymentState(PaymentState.SUCCESS);
                        response.setResult(ClientResultEnum.SUCCESS);
                    } catch (Throwable t) {
                        txt = t.getClass().getSimpleName()
                                + ": "
                                + String.valueOf(t.getMessage());
                    } finally {
                        msg.setDescription(txt);
                        response.setDescription(txt);
                        switch (response.getResult()) {
                            case BUSINESS_FATAL_ERROR:
                            case SYSTEM_FATAL_ERROR:
                            case SYSTEM_ERROR:
                                log.error(txt);
                                break;
                        }
                        e.getIn().setBody(response);
                        if (account != null) {
                            daoAccounts.save(account);
                        }
                        if (payment != null) {
                            payment.setDescription(txt);
                            daoPayments.save(payment);
                        }
                        daoLog.save(msg);
                    }
                }).id("BillingAdaptorProcessor");
    }

}
