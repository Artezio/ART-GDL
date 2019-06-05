/*
 */
package com.artezio.example.billling.adaptor.web;

import com.artezio.example.billling.adaptor.data.entities.BillingClient;
import com.artezio.example.billling.adaptor.data.entities.PaymentRequest;
import com.artezio.example.billling.adaptor.services.ClientsManagement;
import com.artezio.example.billling.adaptor.services.ExampleDataGenerator;
import com.artezio.example.billling.adaptor.services.PaymentsManagement;
import com.artezio.example.billling.adaptor.services.types.PaymentStateCounter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Web user session bean.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@SessionScope
@ManagedBean
@Component
@Slf4j
public class UserSession {

    /**
     * Test payments generation batch size.
     */
    public static final int GEN_SIZE = 20;

    /**
     * Client management service.
     */
    @Autowired
    private ClientsManagement clintsManager;
    /**
     * Example data generation service.
     */
    @Autowired
    private ExampleDataGenerator dataGen;
    /**
     * Payments management service.
     */
    @Autowired
    private PaymentsManagement paymentsManager;
    /**
     * Client list.
     */
    @Getter
    private List<BillingClient> clients;
    /**
     * Payment state counter.
     */
    @Getter
    private PaymentStateCounter stateCounter;
    /**
     * Example payment request.
     */
    private final PaymentRequest examplePayment = new PaymentRequest();

    /**
     * Bean post construct actions.
     */
    @PostConstruct
    public void createSession() {
        log.info("User session created.");
        dataGen.generateClientsIfEmpty();
        clients = new ArrayList<>();
        Set<String> ascSort = new HashSet<>();
        ascSort.add("firstName");
        List<BillingClient> data = clintsManager.getClientPage(0, 1000, ascSort, null);
        if (data != null) {
            clients.addAll(data);
        }
        stateCounter = paymentsManager.countStates();
        examplePayment.setSuccessCount(5);
        if (stateCounter != null && stateCounter.getAll() <= 0) {
            for (BillingClient c : clients) {
                if (c.getId() != null) {
                    examplePayment.setClient(c);
                    dataGen.generatePayments(GEN_SIZE, examplePayment);
                }
            }
            stateCounter = paymentsManager.countStates();
        }
    }

}
