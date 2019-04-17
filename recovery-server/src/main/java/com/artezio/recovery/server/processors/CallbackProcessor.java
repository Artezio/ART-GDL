/*
 */
package com.artezio.recovery.server.processors;

import com.artezio.recovery.server.data.access.IRecoveryOrderCrud;
import com.artezio.recovery.server.data.messages.ClientResponse;
import com.artezio.recovery.server.data.messages.RecoveryOrder;
import com.artezio.recovery.server.data.types.ClientResultEnum;
import com.artezio.recovery.server.data.types.PauseConfig;
import com.artezio.recovery.server.data.types.ProcessingCodeEnum;
import com.artezio.recovery.server.data.types.RecoveryStatusEnum;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
     * Access to the current Apache Camel context.
     */
    @Autowired
    private CamelContext camel;

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
                success = checkPause(order);
                if (!success) {
                    break main;
                }
                success = checkParentQueue(order);
                if (!success) {
                    break main;
                }
                success = checkQueue(order);
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
        boolean success = false;
        Date now = new Date(System.currentTimeMillis());
        Date from = order.getProcessingFrom();
        Date to = order.getProcessingTo();
        if (to != null && now.after(to)) {
            order.setOrderModified(null);
            order.setCode(ProcessingCodeEnum.EXPIRED_BY_DATE);
            order.setDescription("Order is expired by date interval.");
            if (deliveryExpired) {
                success = true;
            } else {
                success = false;
                order.setStatus(RecoveryStatusEnum.ERROR);
                printInfo(order, exchange);
            }
        } else if (from != null && now.before(from)) {
            success = false;
        } else if (from != null && now.after(from)) {
            success = true;
        } else if (to != null && now.before(to)) {
            success = true;
        }
        return success;
    }

    /**
     * Check processing by pause configuration.
     *
     * @param order Recovery order data record.
     * @return False if processing should be interrupted.
     * @throws Exception @see Exception
     */
    private boolean checkPause(RecoveryOrder order) throws Exception {
        boolean success = false;
        String pauseRule = order.getPause();
        if (pauseRule != null) {
            Integer count = order.getProcessingCount();
            PauseConfig pauseConfig = new PauseConfig(pauseRule);
            int timeout = pauseConfig.getInterval(count);
            Date modified = order.getOrderModified();
            Date nextTryFrom = new Date(modified.getTime() + timeout);
            Date now = new Date(System.currentTimeMillis());
            if (now.after(nextTryFrom)) {
                success = true;
            }
        } else {
            success = true;
        }
        return success;
    }

    /**
     * Check processing by queue sequence.
     *
     * @param order Recovery order data record.
     * @return False if processing should be interrupted.
     * @throws Exception @see Exception
     */
    private boolean checkQueue(RecoveryOrder order) throws Exception {
        boolean success = false;
        Page<RecoveryOrder> page = dao.findTopOfQueue(
                PageRequest.of(0, 1),
                order.getQueue(),
                order.getOrderCreated());
        if (page == null || page.isEmpty()) {
            success = true;
        }
        return success;
    }

    /**
     * Check processing by parent queue sequence.
     *
     * @param order Recovery order data record.
     * @return False if processing should be interrupted.
     * @throws Exception @see Exception
     */
    private boolean checkParentQueue(RecoveryOrder order) throws Exception {
        boolean success = false;
        Page<RecoveryOrder> page = dao.findParentQueue(
                PageRequest.of(0, 1),
                order.getQueueParent());
        if (page == null || page.isEmpty()) {
            success = true;
        }
        return success;
    }

    /**
     * Process the recovery order to the callback route.
     *
     * @param order Recovery order data record.
     * @param exchange Apache Camel ESB exchange message.
     * @throws Exception @see Exception
     */
    private void processOrder(RecoveryOrder order, Exchange exchange) throws Exception {
        boolean expired = false;
        switch (order.getCode()) {
            case EXPIRED_BY_DATE:
            case EXPIRED_BY_NUMBER:
                expired = true;
                break;
        }
        Endpoint endpoint = camel.getEndpoint(order.getCallbackUri());
        main:
        {
            if (endpoint == null) {
                order.setCode(ProcessingCodeEnum.FATAL_NO_ENDPOINT_FOUND);
                order.setDescription("Recovery callback endpoint is not found.");
                order.setStatus(RecoveryStatusEnum.ERROR);
                break main;
            }
            ProducerTemplate producer = camel.createProducerTemplate();
            Object obj = producer.requestBody(endpoint, order);
            ClientResponse response;
            if (obj instanceof ClientResponse) {
                response = (ClientResponse) obj;
            } else if (obj == null) {
                response = new ClientResponse();
                response.setResult(ClientResultEnum.SUCCESS);
            } else {
                order.setCode(ProcessingCodeEnum.FATAL_WRONG_RESPONSE);
                order.setDescription("Recovery callback response has a wrong type: "
                        + obj.getClass().getCanonicalName());
                order.setStatus(RecoveryStatusEnum.ERROR);
                break main;
            }
            order.setDescription(response.getDescription());
            if (expired) {
                order.setStatus(RecoveryStatusEnum.ERROR);
                break main;
            }
            switch (response.getResult()) {
                case SUCCESS:
                    if (obj == null) {
                        order.setCode(ProcessingCodeEnum.SUCCESS_DELIVERY);
                    } else {
                        order.setCode(ProcessingCodeEnum.SUCCESS_CLIENT);
                    }
                    order.setStatus(RecoveryStatusEnum.SUCCESS);
                    break;
                case SYSTEM_ERROR:
                    order.setCode(ProcessingCodeEnum.ERROR_CLIENT);
                    order.setStatus(RecoveryStatusEnum.PROCESSING);
                    break;
                case BUSINESS_ERROR:
                    order.setCode(ProcessingCodeEnum.ERROR_BUSINESS);
                    order.setStatus(RecoveryStatusEnum.PROCESSING);
                    break;
                case SYSTEM_FATAL_ERROR:
                    order.setCode(ProcessingCodeEnum.FATAL_CLIENT);
                    order.setStatus(RecoveryStatusEnum.ERROR);
                    break;
                case BUSINESS_FATAL_ERROR:
                    order.setCode(ProcessingCodeEnum.FATAL_BUSINESS);
                    order.setStatus(RecoveryStatusEnum.ERROR);
                    break;
            }
        }
        order.setOrderModified(null);
        printInfo(order, exchange);
    }

    /**
     * Print information message to the LOG.
     *
     * @param order Recovery order data record.
     * @param exchange Apache Camel ESB exchange message.
     * @throws Exception @see Exception
     */
    private void printInfo(RecoveryOrder order, Exchange exchange) throws Exception {
        StringBuilder msg = new StringBuilder();
        msg.append("exchangeId=").append(exchange.getExchangeId()).append(";");
        msg.append("recoveryId=").append(order.getId()).append(";");
        msg.append("externalId=").append(order.getExternalId()).append(";");
        msg.append("status=").append(order.getStatus()).append(";");
        msg.append("code=").append(order.getCode()).append(";");
        msg.append("callbackUri=").append(order.getCallbackUri()).append(";");
        msg.append(" ").append(order.getDescription());
        switch (order.getStatus()) {
            case SUCCESS:
                log.info(msg.toString());
                break;
            case PROCESSING:
                log.debug(msg.toString());
                break;
            case ERROR:
                log.error(msg.toString());
                break;
        }
    }

}
