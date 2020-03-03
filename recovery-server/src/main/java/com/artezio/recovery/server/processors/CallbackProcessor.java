package com.artezio.recovery.server.processors;

import com.artezio.recovery.server.config.PauseConfig;
import com.artezio.recovery.server.data.model.ClientResponse;
import com.artezio.recovery.server.data.model.RecoveryOrder;
import com.artezio.recovery.server.data.repository.RecoveryOrderRepository;
import com.artezio.recovery.server.data.types.ClientResultEnum;
import com.artezio.recovery.server.data.types.HoldingCodeEnum;
import com.artezio.recovery.server.data.types.ProcessingCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.*;
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

import java.util.Date;
import java.util.UUID;

import static com.artezio.recovery.server.data.types.ProcessingCodeEnum.EXPIRED_BY_DATE;
import static com.artezio.recovery.server.data.types.ProcessingCodeEnum.EXPIRED_BY_NUMBER;
import static com.artezio.recovery.server.data.types.RecoveryStatusEnum.ERROR;
import static com.artezio.recovery.server.data.types.RecoveryStatusEnum.SUCCESS;

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
    private RecoveryOrderRepository repository;
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
        RecoveryOrder order = retrieveOrder(exchange);
        if (order != null) {
            order.setCode(ProcessingCodeEnum.REVIEWED);
            if (validateOrder(order)) {
                processOrder(order);
            }
            if (order.getStatus().equals(SUCCESS) || order.getStatus().equals(ERROR)) {
                order.setLockerVersion(UUID.randomUUID().toString());
            }
            Date now = new Date(System.currentTimeMillis());
            if (order.getOrderModified() == null) {
                order.setOrderModified(now);
            }
            order.setOrderUpdated(now);
            order.setVersionId(null);
            repository.save(order);
        }
    }

    /**
     * Validate order for processing to the callback route.
     *
     * @param order Recovery order data record.
     * @return False if processing should be interrupted.
     * @throws Exception @see Exception
     */
    private boolean validateOrder(RecoveryOrder order) throws Exception {
        return checkDateInterval(order) && checkPause(order) && checkQueue(order) && checkCountLimit(order);
    }

    /**
     * Retrieve an recovery order data record from the ESB exchange message.
     *
     * @param exchange Apache Camel ESB exchange message.
     * @return Recovery order data record.
     * @throws Exception @see Exception
     */
    private RecoveryOrder retrieveOrder(Exchange exchange) throws Exception {
        RecoveryOrder order = null;
        Object body = exchange.getIn().getBody();
        if (body instanceof RecoveryOrder) {
            order = (RecoveryOrder) body;
            order = repository.findOrderByVersionId(order.getVersionId());
        } else {
            StringBuilder logMessage = new StringBuilder(exchange.getExchangeId());
            if (body == null) {
                logMessage.append(": Exchange body is null.");
            } else {
                logMessage.append(": Wrong exchange body type: ");
                logMessage.append(body.getClass().getCanonicalName());
            }
            log.error(logMessage.toString());
        }
        return order;
    }

    /**
     * Check count number for limitation of processing tries.
     *
     * @param order Recovery order data record.
     * @return False if processing should be interrupted.
     * @throws Exception @see Exception
     */
    private boolean checkCountLimit(RecoveryOrder order) throws Exception {
        order.setHoldingCode(HoldingCodeEnum.NO_HOLDING);
        boolean success = true;
        Integer count = order.getProcessingCount();
        order.setProcessingCount(++count);
        Integer limit = order.getProcessingLimit();
        if (limit != null && Integer.compare(count, limit) > 0) {
            order.setOrderModified(null);
            order.setCode(EXPIRED_BY_NUMBER);
            order.setDescription("Order is expired by number of tries.");
            if (!deliveryExpired) {
                order.setStatus(ERROR);
                success = false;
                printInfo(order);
            }
        }
        return success;
    }

    /**
     * Check processing date interval.
     *
     * @param order Recovery order data record.
     * @return False if processing should be interrupted.
     * @throws Exception @see Exception
     */
    private boolean checkDateInterval(RecoveryOrder order) throws Exception {
        order.setHoldingCode(HoldingCodeEnum.HOLDING_BY_DATE);
        boolean success = false;
        Date now = new Date(System.currentTimeMillis());
        Date from = order.getProcessingFrom();
        Date to = order.getProcessingTo();
        if (from == null && to == null) {
            success = true;
        } else if (to != null && now.after(to)) {
            order.setOrderModified(null);
            order.setCode(EXPIRED_BY_DATE);
            order.setDescription("Order is expired by date interval.");
            if (deliveryExpired) {
                success = true;
            } else {
                success = false;
                order.setStatus(ERROR);
                printInfo(order);
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
        order.setHoldingCode(HoldingCodeEnum.HOLDING_BY_PAUSE);
        boolean success = true;
        String pauseRule = order.getPause();
        if (pauseRule != null) {
            Integer count = order.getProcessingCount() + 1;
            PauseConfig pauseConfig = new PauseConfig(pauseRule);
            int timeout = pauseConfig.getInterval(count) * 1000;
            Date modified = order.getOrderModified();
            Date nextTryFrom = new Date(modified.getTime() + timeout);
            Date now = new Date(System.currentTimeMillis());
            success = now.after(nextTryFrom);
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
        order.setHoldingCode(HoldingCodeEnum.HOLDING_BY_QUEUE);
        boolean success = true;
        if (order.getQueue() != null || order.getQueueParent() != null) {
            Page<RecoveryOrder> page = repository.findTopOfQueue(
                    PageRequest.of(0, 1),
                    order.getQueue(),
                    order.getQueueParent(),
                    order.getOrderCreated());
            if (page != null && !page.isEmpty()) {
                success = false;
                RecoveryOrder top = page.getContent().get(0);
                if (order.getQueue() != null && order.getQueue().equals(top.getQueue())) {
                    order.setHoldingCode(HoldingCodeEnum.HOLDING_BY_QUEUE);
                } else {
                    order.setHoldingCode(HoldingCodeEnum.HOLDING_BY_PARENT_QUEUE);
                }
            }
        }
        return success;
    }

    /**
     * Process the recovery order to the callback route.
     *
     * @param order Recovery order data record.
     * @throws Exception @see Exception
     */
    @SuppressWarnings("ThrowableResultIgnored")
    private void processOrder(RecoveryOrder order) throws Exception {
        boolean expired = false;
        if (order.getCode().equals(EXPIRED_BY_DATE) || (order.getCode().equals(EXPIRED_BY_NUMBER))) {
            expired = true;
        }
        main:
        {
            ProducerTemplate producer = camel.createProducerTemplate();
            ClientResponse response;
            Object responseBody;
            try {
                responseBody = producer.requestBody(order.getCallbackUri(), order);
            } catch (CamelExecutionException e) {
                String executionError =
                        (e.getCause() != null ? e.getCause().getClass().getSimpleName() : e.getClass().getSimpleName())
                                + ": "
                                + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                order.setDescription(executionError);
                order.setCode(ProcessingCodeEnum.ERROR_DELIVERY);
                break main;
            }
            if (responseBody instanceof ClientResponse) {
                response = (ClientResponse) responseBody;
            } else if (responseBody == null || responseBody instanceof RecoveryOrder) {
                response = new ClientResponse();
                response.setResult(ClientResultEnum.SUCCESS);
            } else {
                order.setCode(ProcessingCodeEnum.FATAL_WRONG_RESPONSE);
                order.setDescription("Recovery callback response has a wrong type: "
                        + responseBody.getClass().getCanonicalName());
                order.setStatus(ERROR);
                break main;
            }
            order.setDescription(response.getDescription());
            if (expired) {
                order.setStatus(ERROR);
                break main;
            }
            processResponseResult(response.getResult(), responseBody, order);
        }
        order.setOrderModified(null);
        printInfo(order);
    }

    private void processResponseResult(ClientResultEnum result, Object responseBody, RecoveryOrder order) {
        switch (result) {
            case SUCCESS:
                if (responseBody instanceof ClientResponse) {
                    order.setCode(ProcessingCodeEnum.SUCCESS_CLIENT);
                } else {
                    order.setCode(ProcessingCodeEnum.SUCCESS_DELIVERY);
                }
                order.setStatus(SUCCESS);
                break;
            case SYSTEM_ERROR:
                order.setCode(ProcessingCodeEnum.ERROR_CLIENT);
                break;
            case BUSINESS_ERROR:
                order.setCode(ProcessingCodeEnum.ERROR_BUSINESS);
                break;
            case SYSTEM_FATAL_ERROR:
                order.setCode(ProcessingCodeEnum.FATAL_CLIENT);
                order.setStatus(ERROR);
                break;
            case BUSINESS_FATAL_ERROR:
                order.setCode(ProcessingCodeEnum.FATAL_BUSINESS);
                order.setStatus(ERROR);
                break;
        }
    }

    /**
     * Print information message to the LOG.
     *
     * @param order Recovery order data record.
     * @throws Exception @see Exception
     */
    private void printInfo(RecoveryOrder order) throws Exception {
        StringBuilder message = new StringBuilder();
        message.append("recoveryId=").append(order.getId()).append("; ");
        message.append("externalId=").append(order.getExternalId()).append("; ");
        message.append("status=").append(order.getStatus()).append("; ");
        message.append("code=").append(order.getCode()).append("; ");
        message.append("queue=").append(order.getQueue()).append("; ");
        message.append("queueParent=").append(order.getQueueParent()).append("; ");
        message.append("callbackUri=").append(order.getCallbackUri()).append("; ");
        message.append(order.getDescription());
        switch (order.getStatus()) {
            case SUCCESS:
                log.info(message.toString());
                break;
            case PROCESSING:
                log.debug(message.toString());
                break;
            case ERROR:
                log.error(message.toString());
                break;
        }
    }

}
