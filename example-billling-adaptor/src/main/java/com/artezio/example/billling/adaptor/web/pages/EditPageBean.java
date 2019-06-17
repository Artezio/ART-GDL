/*
 */
package com.artezio.example.billling.adaptor.web.pages;

import com.artezio.example.billling.adaptor.data.entities.BillingClient;
import com.artezio.example.billling.adaptor.data.entities.PaymentRequest;
import com.artezio.example.billling.adaptor.data.types.PaymentOperationType;
import com.artezio.example.billling.adaptor.services.ClientsManagement;
import com.artezio.example.billling.adaptor.services.PaymentsManagement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.ManagedBean;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@SessionScope
@ManagedBean
@Component
@Slf4j
public class EditPageBean {

    /**
     * Payments management service.
     */
    @Autowired
    private PaymentsManagement paymentsManager;
    /**
     * Client management service.
     */
    @Autowired
    private ClientsManagement clintsManager;
    /**
     * Index page access session bean.
     */
    @Autowired
    private IndexPageBean indexPage;

    /**
     * Current payment request.
     */
    @Getter
    private PaymentRequest payment;
    /**
     * Client list.
     */
    @Getter
    private List<BillingClient> clients;
    /**
     * Selected client ID.
     */
    @Getter
    @Setter
    private Long clientId;
    /**
     * Selected payment request type name.
     */
    @Getter
    @Setter
    private String requestType;

    /**
     * Set current payment request and go to the edit page.
     *
     * @param payment Payment request.
     * @return Edit page URL.
     */
    public String editPayment(PaymentRequest payment) {
        if (payment != null) {
            this.payment = payment;
            if (payment.getClient() != null) {
                clientId = payment.getClient().getId();
            }
        } else {
            this.payment = new PaymentRequest();
            this.payment.setSuccessCount(5);
        }
        clients = new ArrayList<>(10);
        Set<String> ascSort = new HashSet<>();
        ascSort.add("firstName");
        List<BillingClient> data = clintsManager.getClientPage(0, 1000, ascSort, null);
        if (data != null) {
            clients.addAll(data);
        }
        if (this.payment.getClient() == null && !clients.isEmpty()) {
            this.payment.setClient(clients.get(0));
            clientId = this.payment.getClient().getId();
        }
        if (this.payment.getOperationType() == null) {
            this.payment.setOperationType(PaymentOperationType.ENROLL_PAYMENT);
        }
        requestType = this.payment.getOperationType().name();
        return "edit?faces-redirect=true";
    }

    /**
     * Set current payment request and go to the index page.
     *
     * @return Index page URL.
     */
    public String savePayment() {
        main:
        {
            if (payment == null) {
                break main;
            }
            if (clientId == null) {
                break main;
            }
            BillingClient client = clients.stream()
                    .filter(c -> clientId.equals(c.getId()))
                    .findAny()
                    .orElse(null);
            if (client == null) {
                break main;
            }
            payment.setClient(client);
            if (requestType == null) {
                break main;
            }
            this.payment.setOperationType(PaymentOperationType.valueOf(requestType));
            paymentsManager.save(payment);
        }
        indexPage.loadViewData();
        return "index?faces-redirect=true";
    }

    /**
     * Set default queue ID.
     */
    public void syncQueue() {
        BillingClient client = clients.stream()
                .filter(c -> clientId.equals(c.getId()))
                .findAny()
                .orElse(null);
        if (client != null) {
            this.payment.setQueue("client-id-" + String.valueOf(client.getId()));
        }
    }
}
