/*
 */
package com.artezio.recovery.server.processors;

import com.artezio.recovery.server.data.access.IRecoveryOrderCrud;
import com.artezio.recovery.server.data.messages.RecoveryOrder;
import com.artezio.recovery.server.data.messages.RecoveryRequest;
import com.artezio.recovery.server.data.types.PauseConfig;
import com.artezio.recovery.server.data.types.ProcessingCodeEnum;
import com.artezio.recovery.server.data.types.RecoveryException;
import com.artezio.recovery.server.data.types.RecoveryStatusEnum;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Recovery request storing processor.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Slf4j
public class StoringProcessor implements Processor {

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
     * Recovery request storing process definition.
     *
     * @param exchange Apache Camel ESB exchange message.
     * @throws Exception @see Exception
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.MANDATORY)
    public void process(Exchange exchange) throws Exception {
        StringBuilder logMsg = new StringBuilder(exchange.getExchangeId());
        Object exBody = exchange.getIn().getBody();
        if (exBody instanceof RecoveryRequest) {
            RecoveryRequest request = (RecoveryRequest) exBody;
            RecoveryOrder newOrder = makeNewOrder(exchange, request);
            RecoveryOrder storedOrder = dao.save(newOrder);
            exchange.getIn().setBody(storedOrder);
        } else if (exBody == null) {
            logMsg.append(": No income request found.");
            RecoveryException r = new RecoveryException(logMsg.toString());
            throw r;
        } else {
            logMsg.append(": Wrong type of income request: ");
            logMsg.append(exBody.getClass().getCanonicalName());
            RecoveryException r = new RecoveryException(logMsg.toString());
            throw r;
        }
    }

    /**
     * Make DB order record from recovery client request.
     *
     * @param exchange Apache Camel ESB exchange message.
     * @param request Recovery client request message.
     * @return Recovery DB order record.
     * @throws Exception @see Exception
     */
    private RecoveryOrder makeNewOrder(Exchange exchange, RecoveryRequest request)
            throws Exception {
        StringBuilder logMsg = new StringBuilder(exchange.getExchangeId());
        if (request.getCallbackId() == null) {
            logMsg.append(": Callback route ID is mandatory.");
            RecoveryException r = new RecoveryException(logMsg.toString());
            throw r;
        }
        Route route = camel.getRoute(request.getCallbackId());
        if (route == null) {
            logMsg.append(": Callback route is not found with callbackId = ");
            logMsg.append(request.getCallbackId());
            RecoveryException r = new RecoveryException(logMsg.toString());
            throw r;
        }
        if (request.getPause() != null && !PauseConfig.checkRule(request.getPause())) {
            logMsg.append(": Wrong pause rule format. Pause rule pattern: ");
            logMsg.append(PauseConfig.PAUSE_RULE_REGEX);
            RecoveryException r = new RecoveryException(logMsg.toString());
            throw r;
        }
        Date now = new Date(System.currentTimeMillis());
        RecoveryOrder order = new RecoveryOrder();
        order.setCallbackId(request.getCallbackId());
        order.setCode(ProcessingCodeEnum.NEW);
        order.setDescription("Order stored.");
        order.setExternalId(request.getExternalId());
        order.setLocker(request.getLocker() == null
                ? UUID.randomUUID().toString()
                : request.getLocker());
        order.setLockerUp(Boolean.TRUE);
        order.setMessage(request.getMessage());
        order.setOrderCreated(now);
        order.setOrderModified(now);
        order.setOrderOpened(now);
        order.setOrderUpdated(now);
        order.setPause(request.getPause());
        order.setProcessingCount(0);
        order.setProcessingFrom(request.getProcessingFrom());
        order.setProcessingLimit(request.getProcessingLimit());
        order.setProcessingTo(request.getProcessingTo());
        order.setQueue(request.getQueue());
        order.setQueueParent(request.getQueueParent());
        order.setStatus(RecoveryStatusEnum.PROCESSING);
        order.setVersionId(null);
        return order;
    }
}
