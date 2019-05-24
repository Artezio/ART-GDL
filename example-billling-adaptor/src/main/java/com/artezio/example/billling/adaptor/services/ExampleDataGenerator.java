/*
 */
package com.artezio.example.billling.adaptor.services;

import com.artezio.example.billling.adaptor.data.access.IBillingClientCrud;
import com.artezio.example.billling.adaptor.data.access.IPaymentRequestCrud;
import com.artezio.example.billling.adaptor.data.entities.BillingClient;
import com.artezio.example.billling.adaptor.data.entities.PaymentRequest;
import com.artezio.example.billling.adaptor.data.types.PaymentOperationType;
import com.artezio.example.billling.adaptor.data.types.PaymentState;
import java.math.BigDecimal;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Batch payments generation operations.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Service
public class ExampleDataGenerator {

    /**
     * Billing accounts data access object.
     */
    @Autowired
    private IBillingClientCrud daoClients;
    /**
     * Payment requests data access object.
     */
    @Autowired
    private IPaymentRequestCrud daoPayments;
    /**
     * Clients management operations.
     */
    @Autowired
    private ClientsManagement clients;
    

    /**
     * Generate default billing client if there is no one in the DB.
     */
    @Transactional
    public void generateClientsIfEmpty() {
        long count = daoClients.count();
        if (count == 0) {
            clients.appendClient("Alice", "Fox");
            clients.appendClient("Oliver", "Smith");
            clients.appendClient("George", "Wilson");
            clients.appendClient("Amelia", "Miller");
            clients.appendClient("Emily", "Anderson");
            clients.appendClient("Daniel", "Murphy");
            clients.appendClient("Harry", "Roberts");
            clients.appendClient("Megan", "Brown");
            clients.appendClient("Emma", "Williams");
            clients.appendClient("Sophie", "Wang");
        }
    }

    /**
     * Generate payment requests using an example.
     *
     * @param count Number of generating requests.
     * @param payment Example payment request.
     */
    @Transactional
    public void generatePayments(int count, PaymentRequest payment) {
        if (count <= 0 || payment == null) {
            return;
        }
        final long clientsTotal = daoClients.count();
        final Random random = new Random();
        final BigDecimal hundred = BigDecimal.valueOf(100);
        for (int i = 0; i < count; i++) {
            int randClientNum = random.nextInt((int) clientsTotal);
            Page<BillingClient> clientPage = daoClients.findAll(PageRequest.of(randClientNum, 1));
            if (clientPage.isEmpty()) {
                continue;
            }
            BillingClient client = clientPage.getContent().get(0);
            BigDecimal amount = BigDecimal.valueOf(random.nextInt(10000));
            amount = amount.divide(hundred);
            PaymentRequest request = new PaymentRequest();
            request.setAmount(amount);
            request.setClient(client);
            request.setDescription("Registered");
            request.setLocker(payment.getLocker());
            request.setOperationType(PaymentOperationType.ENROLL_PAYMENT);
            request.setPause(payment.getPause());
            request.setPaymentState(PaymentState.REGISTERED);
            request.setProcessingFrom(payment.getProcessingFrom());
            request.setProcessingLimit(payment.getProcessingLimit());
            request.setProcessingTo(payment.getProcessingTo());
            request.setQueue("client-id-" + String.valueOf(payment.getClient().getId()));
            request.setQueueParent(payment.getQueueParent());
            request.setSuccessCount(payment.getSuccessCount());
            daoPayments.save(request);
        }
    }

}
