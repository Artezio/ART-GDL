package com.artezio.recovery.kafka.processor;

import com.artezio.recovery.kafka.model.KafkaRecoveryRequest;
import com.artezio.recovery.server.data.messages.RecoveryRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.function.Function;

import static com.artezio.recovery.kafka.route.KafkaRoute.KAFKA_CALLBACK_ROUTE_URL;

/**
 * Recovery request processor.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Slf4j
public class KafkaRecoveryProcessor implements Processor {

    private Function<KafkaRecoveryRequest, RecoveryRequest> extractRecoveryRequest = kafkaRecoveryRequest -> {
        RecoveryRequest request = new RecoveryRequest();
        request.setCallbackUri(KAFKA_CALLBACK_ROUTE_URL);
        request.setExternalId(kafkaRecoveryRequest.getExternalId());
        request.setLocker(kafkaRecoveryRequest.getLocker());
        request.setMessage(kafkaRecoveryRequest.getMessage());
        request.setPause(kafkaRecoveryRequest.getPause());
        request.setProcessingFrom(kafkaRecoveryRequest.getProcessingFrom());
        request.setProcessingLimit(kafkaRecoveryRequest.getProcessingLimit());
        request.setProcessingTo(kafkaRecoveryRequest.getProcessingTo());
        request.setQueue(kafkaRecoveryRequest.getQueue());
        request.setQueueParent(kafkaRecoveryRequest.getQueueParent());
        return request;
    };

    @Override
    public void process(Exchange exchange) throws Exception {
        String request = exchange.getIn().getBody(String.class);
        KafkaRecoveryRequest kafkaRequest = new ObjectMapper().readValue(request, KafkaRecoveryRequest.class);
        exchange.getIn().setBody(extractRecoveryRequest.apply(kafkaRequest));
    }
}
