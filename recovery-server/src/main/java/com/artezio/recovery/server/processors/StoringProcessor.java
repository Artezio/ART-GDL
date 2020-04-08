package com.artezio.recovery.server.processors;

import com.artezio.recovery.server.config.PauseConfig;
import com.artezio.recovery.server.data.exception.RecoveryException;
import com.artezio.recovery.server.data.model.RecoveryRequest;
import com.artezio.recovery.server.data.model.RecoveryOrder;
import com.artezio.recovery.server.data.repository.RecoveryOrderRepository;
import com.artezio.recovery.server.data.types.HoldingCodeEnum;
import com.artezio.recovery.server.data.types.ProcessingCodeEnum;
import com.artezio.recovery.server.data.types.RecoveryStatusEnum;
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
    private RecoveryOrderRepository repository;

    /**
     * Recovery request storing process definition.
     *
     * @param exchange Apache Camel ESB exchange message.
     * @throws Exception @see Exception
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.MANDATORY)
    public void process(Exchange exchange) throws Exception {
        StringBuilder logMessage = new StringBuilder(exchange.getExchangeId());
        Object exBody = exchange.getIn().getBody();
        if (exBody instanceof RecoveryRequest) {
            RecoveryRequest request = (RecoveryRequest) exBody;
            RecoveryOrder order = createRecoveryOrder(exchange, request);
            RecoveryOrder storedOrder = repository.save(order);
            exchange.getIn().setBody(storedOrder);
        } else if (exBody == null) {
            logMessage.append(": No income request found.");
            throw new RecoveryException(logMessage.toString());
        } else {
            logMessage.append(": Wrong type of income request: ");
            logMessage.append(exBody.getClass().getCanonicalName());
            throw new RecoveryException(logMessage.toString());
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
    private RecoveryOrder createRecoveryOrder(Exchange exchange, RecoveryRequest request) throws Exception {
        StringBuilder logMessage = new StringBuilder(exchange.getExchangeId());
        if (request.getCallbackUri() == null) {
            logMessage.append(": Callback route URI is mandatory.");
            throw new RecoveryException(logMessage.toString());
        }
        String pauseRule = request.getPause() != null
                ? request.getPause().replaceAll("\\s+", "")
                : "";
        if (!(pauseRule.isEmpty() || PauseConfig.checkRule(pauseRule))) {
            logMessage.append(": Wrong pause rule (");
            logMessage.append(pauseRule);
            logMessage.append(") format. Pause rule pattern: ");
            logMessage.append(PauseConfig.PAUSE_RULE_REGEX);
            throw new RecoveryException(logMessage.toString());
        }
        Date now = new Date(System.currentTimeMillis());
        return RecoveryOrder.builder()
                .callbackUri(request.getCallbackUri())
                .code(ProcessingCodeEnum.NEW)
                .description("Order stored.")
                .externalId(request.getExternalId())
                .holdingCode(HoldingCodeEnum.NO_HOLDING)
                .locker(request.getLocker() == null ? UUID.randomUUID().toString() : request.getLocker())
                .lockerVersion(new UUID(0, 0).toString())
                .message(request.getMessage())
                .orderCreated(now)
                .orderModified(now)
                .orderOpened(now)
                .orderUpdated(now)
                .pause(pauseRule.isEmpty() ? null : pauseRule)
                .processingCount(0)
                .processingFrom(request.getProcessingFrom())
                .processingLimit(request.getProcessingLimit())
                .processingTo(request.getProcessingTo())
                .queue(request.getQueue())
                .queueParent(request.getQueueParent())
                .status(RecoveryStatusEnum.PROCESSING)
                .versionId(null).build();
    }
}
