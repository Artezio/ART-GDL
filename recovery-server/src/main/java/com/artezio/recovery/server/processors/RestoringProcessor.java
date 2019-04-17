/*
 */
package com.artezio.recovery.server.processors;

import com.artezio.recovery.server.data.access.IRecoveryOrderCrud;
import com.artezio.recovery.server.data.messages.RecoveryOrder;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Recovery request restoring processor.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Slf4j
public class RestoringProcessor implements Processor {

    /**
     * Data access object.
     */
    @Autowired
    private IRecoveryOrderCrud dao;

    /**
     * Recovery request restoring process definition.
     *
     * @param exchange Apache Camel ESB exchange message.
     * @throws Exception @see Exception
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
    public void process(Exchange exchange) throws Exception {
        RecoveryOrder order;
        Page<RecoveryOrder> page;
        search:
        {
            page = dao.findNewOrders(PageRequest.of(0, 1));
            order = lockOrder(page);
            if (order != null) {
                break search;
            }
            page = dao.findProcessingOrders(PageRequest.of(0, 1));
            order = lockOrder(page);
            if (order != null) {
                break search;
            }
            page = dao.findQueuedOrders(PageRequest.of(0, 1));
            order = lockOrder(page);
            if (order != null) {
                break search;
            }
        }
        exchange.getIn().setBody(order);
    }

    /**
     * Lock first recovery order from data page.
     *
     * @param page Data page of recovery orders.
     * @return Locked recovery order.
     * @throws Exception Exception @see Exception
     */
    private RecoveryOrder lockOrder(Page<RecoveryOrder> page) throws Exception {
        RecoveryOrder order = null;
        if (page != null && !page.isEmpty()) {
            order = page.getContent().get(0);
            try {
                order = dao.findAndLock(order.getId());
                if (order != null) {
                    order.setOrderOpened(new Date(System.currentTimeMillis()));
                    order.setVersionId(UUID.randomUUID().toString());
                    dao.save(order);
                }
            } catch (Throwable t) {
                log.trace(t.getClass().getSimpleName() 
                        + ": " 
                        + String.valueOf(t.getMessage()));
                order = null;
            }
        }
        return order;
    }

}
