/*
 */
package com.artezio.example.billling.adaptor.services;

import com.artezio.example.billling.adaptor.data.access.IBillingLogCrud;
import com.artezio.example.billling.adaptor.data.entities.BillingLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Billing log management operations.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Service
public class BillingLogManagement {

    /**
     * Payment requests data access object.
     */
    @Autowired
    private IBillingLogCrud daoLog;

    /**
     * Get a page of billing log records.
     *
     * @param pageNumber Data page number.
     * @param pageSize Data page size.
     * @param ascSorting Data page ascending sorting fields.
     * @param descSorting Data page descending sorting fields.
     * @return Data page of billing log records.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public List<BillingLog> getLogPage(int pageNumber, int pageSize, 
            Set<String> ascSorting, Set<String> descSorting) {
        List<BillingLog> bLogs = new ArrayList<>();
        List<Sort.Order> orders  = new ArrayList<>();
        if (ascSorting != null) {
            ascSorting.forEach((field) -> {
                orders.add(Sort.Order.asc(field));
            });
        }
        if (descSorting != null) {
            descSorting.forEach((field) -> {
                orders.add(Sort.Order.desc(field));
            });
        }
        Page<BillingLog> page = daoLog.findAll(
                PageRequest.of(
                        pageNumber, 
                        pageSize,
                        Sort.by(orders)
                        ));
        bLogs.addAll(page.getContent());
        return bLogs;
    }
   
    /**
     * Remove all billing log records from the DB.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void removeAll() {
        daoLog.deleteAll();
    }
    
    /**
     * Count all billing log records.
     * 
     * @return Number of all billing log records in the DB.
     */
    public long count() {
        return daoLog.count();
    }
    
}
