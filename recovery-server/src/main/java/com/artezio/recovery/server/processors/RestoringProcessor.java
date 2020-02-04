package com.artezio.recovery.server.processors;

import com.artezio.recovery.server.data.repository.RecoveryOrderRepository;
import com.artezio.recovery.server.data.model.RecoveryOrder;

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
    private RecoveryOrderRepository repository;

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
        page = repository.findNewOrders(PageRequest.of(0, 1), new Date());
        order = lockOrder(page);
        if (order == null) {
            page = repository.findProcessingOrders(PageRequest.of(0, 1), new Date());
            order = lockOrder(page);
            if (order == null) {
                page = repository.findQueuedOrders(PageRequest.of(0, 1), new Date());
                order = lockOrder(page);
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
            order.setOrderOpened(new Date(System.currentTimeMillis()));
            order.setVersionId(UUID.randomUUID().toString());
            int updated = 0;
            try {
                updated = repository.updateVersion(order.getId(), order.getVersionId(), new Date());
            } catch (Throwable t) {
                log.trace(t.getClass().getSimpleName() + ": " + t.getMessage());
            } finally {
                order = (updated > 0) ? order : null;
            }
        }
        return order;
    }
}
