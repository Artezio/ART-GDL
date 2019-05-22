/*
 */
package com.artezio.example.billling.adaptor.services;

import com.artezio.example.billling.adaptor.data.access.IBillingClientCrud;
import com.artezio.example.billling.adaptor.data.entities.BillingClient;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Clients management operations.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Service
public class ClientsManagement {

    /**
     * Billing accounts data access object.
     */
    @Autowired
    private IBillingClientCrud daoClients;
    
    /**
     * Get a page of client records.
     * 
     * @param pageNumber Data page number.
     * @param pageSize Data page size.
     * @return Data page of clients records.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public List<BillingClient> getClientPage(int pageNumber, int pageSize) {
        List<BillingClient> clients = new ArrayList<>();
        Page<BillingClient> page = daoClients.getPage(PageRequest.of(pageNumber, pageSize));
        clients.addAll(page.getContent());
        return clients;
    }
    
    /**
     * Create or update client record to the DB.
     * 
     * @param client Client record object.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void save(BillingClient client) {
        if (client == null) {
            return;
        }
        daoClients.save(client);
    }

    /**
     * Remove client record from the DB.
     * 
     * @param clientId Client record ID.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void remove(Long clientId) {
        if (clientId == null) {
            return;
        }
        daoClients.deleteById(clientId);
    }
    
}
