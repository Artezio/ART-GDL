package com.artezio.recovery.jms.processor;


import com.artezio.recovery.jms.model.JMSRecoveryRequest;
import com.artezio.recovery.server.data.messages.RecoveryRequest;
import com.artezio.recovery.server.data.types.RecoveryException;
import com.artezio.recovery.storage.processor.RecoveryMessageProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.function.Function;

import static com.artezio.recovery.jms.route.JMSRoute.JMS_CALLBACK_ROUTE_URL;

/**
 * Recovery request processor.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Slf4j
public class JMSRequestProcessor implements Processor {

    /**
     * Processor for storing recovery messages.
     */
    @Autowired
    private RecoveryMessageProcessor messageProcessor;

    private Function<JMSRecoveryRequest, RecoveryRequest> extractRecoveryRequest = jmsRequest -> {
        RecoveryRequest request = new RecoveryRequest();
        request.setCallbackUri(JMS_CALLBACK_ROUTE_URL);
        request.setExternalId(jmsRequest.getExternalId());
        request.setLocker(jmsRequest.getLocker());
        request.setMessage(messageProcessor.processSaving(jmsRequest.getMessage()));
        request.setPause(jmsRequest.getPause());
        request.setProcessingFrom(jmsRequest.getProcessingFrom());
        request.setProcessingLimit(jmsRequest.getProcessingLimit());
        request.setProcessingTo(jmsRequest.getProcessingTo());
        request.setQueue(jmsRequest.getQueue());
        request.setQueueParent(jmsRequest.getQueueParent());
        return request;
    };


    @Override
    public void process(Exchange exchange) throws Exception {
        Object request = exchange.getIn().getBody();
        if (request instanceof JMSRecoveryRequest) {
            exchange.getIn().setBody(extractRecoveryRequest.apply((JMSRecoveryRequest) request));
        } else {
            throw new RecoveryException("Wrong request type");
        }
    }
}
