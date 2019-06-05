/*
 */
package com.artezio.example.billling.adaptor.web;

import com.artezio.example.billling.adaptor.data.entities.BillingClient;
import com.artezio.example.billling.adaptor.services.ClientsManagement;
import com.artezio.example.billling.adaptor.services.ExampleDataGenerator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
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

//    public String getTestMessage() {
//        return "Hello world!";
//    }
    @Autowired
    private ClientsManagement clintsManager;
    @Autowired
    private ExampleDataGenerator dataGen;

    @PostConstruct
    public void createSession() {
        log.info("User session created.");
        dataGen.generateClientsIfEmpty();
    }

    public List<BillingClient> getClients() {
        List<BillingClient> clients = new ArrayList<>();
        Set<String> ascSort = new HashSet<>();
        ascSort.add("firstName");
        List<BillingClient> data = clintsManager.getClientPage(0, 1000, ascSort, null);
        if (data != null) {
            clients.addAll(data);
        }
        return clients;
    }
}
