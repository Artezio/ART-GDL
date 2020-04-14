package com.artezio.recovery.server.processors;

import com.artezio.recovery.server.data.types.*;
import com.artezio.recovery.server.data.access.IRecoveryOrderCrud;
import com.artezio.recovery.server.data.messages.RecoveryOrder;
import com.artezio.recovery.server.data.messages.RecoveryRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

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
     * @param request  Recovery client request message.
     * @return Recovery DB order record.
     * @throws Exception @see Exception
     */
    private RecoveryOrder makeNewOrder(Exchange exchange, RecoveryRequest request)
            throws Exception {
        StringBuilder logMsg = new StringBuilder(exchange.getExchangeId());
        if (request.getCallbackUri() == null) {
            logMsg.append(": Callback route URI is mandatory.");
            RecoveryException r = new RecoveryException(logMsg.toString());
            throw r;
        }
        String pauseRule = request.getPause() != null
                ? request.getPause().replaceAll("\\s+", "")
                : "";
        if (!(pauseRule.isEmpty() || PauseConfig.checkRule(pauseRule))) {
            logMsg.append(": Wrong pause rule (");
            logMsg.append(pauseRule);
            logMsg.append(") format. Pause rule pattern: ");
            logMsg.append(PauseConfig.PAUSE_RULE_REGEX);
            RecoveryException r = new RecoveryException(logMsg.toString());
            throw r;
        }
        Date now = new Date(System.currentTimeMillis());
        RecoveryOrder order = new RecoveryOrder();
        order.setCallbackUri(request.getCallbackUri());
        order.setCode(ProcessingCodeEnum.NEW);
        order.setDescription("Order stored.");
        order.setExternalId(request.getExternalId());
        order.setHoldingCode(HoldingCodeEnum.NO_HOLDING);
        order.setLocker(request.getLocker() == null
                ? UUID.randomUUID().toString()
                : request.getLocker());
        order.setLockerVersion(new UUID(0, 0).toString());
        order.setMessage(request.getMessage());
        order.setOrderCreated(now);
        order.setOrderModified(now);
        order.setOrderOpened(now);
        order.setOrderUpdated(now);
        order.setPause(pauseRule.isEmpty() ? null : pauseRule);
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
