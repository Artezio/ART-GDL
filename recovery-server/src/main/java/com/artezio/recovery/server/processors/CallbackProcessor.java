/*
 */
package com.artezio.recovery.server.processors;

import com.artezio.recovery.server.data.access.IRecoveryOrderCrud;
import com.artezio.recovery.server.data.messages.RecoveryOrder;
import com.artezio.recovery.server.data.types.ProcessingCodeEnum;
import com.artezio.recovery.server.data.types.RecoveryStatusEnum;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Recovery callback processor.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Slf4j
public class CallbackProcessor implements Processor {

    /**
     * Data access object.
     */
    @Autowired
    private IRecoveryOrderCrud dao;

    /**
     * Property of flag to allow processing of expired orders.
     */
    @Value("${com.artezio.recovery.delivery.expired:true}")
    private boolean deliveryExpired;

    /**
     * Recovery callback processing definition.
     *
     * @param exchange Apache Camel ESB exchange message.
     * @throws Exception @see Exception
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
    public void process(Exchange exchange) throws Exception {
        RecoveryOrder order = retriveOrder(exchange);
        if (order != null) {
            boolean success;
            main:
            try {
                success = checkPause(order, exchange);
                if (!success) {
                    break main;
                }
                success = checkParentQueue(order, exchange);
                if (!success) {
                    break main;
                }
                success = checkQueue(order, exchange);
                if (!success) {
                    break main;
                }
                success = checkDateInterval(order, exchange);
                if (!success) {
                    break main;
                }
                success = checkCountLimit(order, exchange);
                if (!success) {
                    break main;
                }
                processOrder(order, exchange);
            } finally {
                Date now = new Date(System.currentTimeMillis());
                if (order.getOrderModified() == null) {
                    order.setOrderModified(now);
                }
                order.setOrderUpdated(now);
                order.setVersionId(null);
                dao.save(order);
            }
        }
    }

    /**
     * Retrieve an recovery order data record from the ESB exchange message.
     *
     * @param exchange Apache Camel ESB exchange message.
     * @return Recovery order data record.
     * @throws Exception @see Exception
     */
    private RecoveryOrder retriveOrder(Exchange exchange) throws Exception {
        RecoveryOrder order = null;
        Object body = exchange.getIn().getBody();
        if (body instanceof RecoveryOrder) {
            order = (RecoveryOrder) body;
        } else {
            StringBuilder logMsg = new StringBuilder(exchange.getExchangeId());
            if (body == null) {
                logMsg.append(": Exhange body is null.");
            } else {
                logMsg.append(": Wrong exhange body type: ");
                logMsg.append(body.getClass().getCanonicalName());
            }
            log.error(logMsg.toString());
        }
        return order;
    }

    /**
     * Check count number for limitation of processing tries.
     * 
     * @param order Recovery order data record.
     * @param exchange Apache Camel ESB exchange message.
     * @return False if processing should be interrupted.
     * @throws Exception @see Exception
     */
    private boolean checkCountLimit(RecoveryOrder order, Exchange exchange) throws Exception {
        boolean success = false;
        Integer count = order.getProcessingCount();
        count = (count == null) ? 0 : count;
        order.setProcessingCount(++count);
        Integer limit = order.getProcessingLimit();
        if (limit != null && Integer.compare(count, limit) > 0) {
            order.setOrderModified(null);
            order.setCode(ProcessingCodeEnum.EXPIRED_BY_NUMBER);
            order.setDescription("Order is expired by number of tries.");
            if (deliveryExpired) {
                success = true;
            } else {
                order.setStatus(RecoveryStatusEnum.ERROR);
                printInfo(order, exchange);
            }
        } else {
            success = true;
        }
        return success;
    }

    /**
     * Check processing date interval.
     * 
     * @param order Recovery order data record.
     * @param exchange Apache Camel ESB exchange message.
     * @return False if processing should be interrupted.
     * @throws Exception @see Exception
     */
    private boolean checkDateInterval(RecoveryOrder order, Exchange exchange) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Check processing by pause configuration.
     * 
     * @param order Recovery order data record.
     * @param exchange Apache Camel ESB exchange message.
     * @return False if processing should be interrupted.
     * @throws Exception @see Exception
     */
    private boolean checkPause(RecoveryOrder order, Exchange exchange) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Check processing by queue sequence.
     * 
     * @param order Recovery order data record.
     * @param exchange Apache Camel ESB exchange message.
     * @return False if processing should be interrupted.
     * @throws Exception @see Exception
     */
    private boolean checkQueue(RecoveryOrder order, Exchange exchange) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Check processing by parent queue sequence.
     * 
     * @param order Recovery order data record.
     * @param exchange Apache Camel ESB exchange message.
     * @return False if processing should be interrupted.
     * @throws Exception @see Exception
     */
    private boolean checkParentQueue(RecoveryOrder order, Exchange exchange) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Process the recovery order to the callback route.
     * 
     * @param order Recovery order data record.
     * @param exchange Apache Camel ESB exchange message.
     * @throws Exception @see Exception
     */
    private void processOrder(RecoveryOrder order, Exchange exchange) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Print information message to the LOG.
     * 
     * @param order Recovery order data record.
     * @param exchange Apache Camel ESB exchange message.
     * @throws Exception @see Exception
     */
    private void printInfo(RecoveryOrder order, Exchange exchange) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
