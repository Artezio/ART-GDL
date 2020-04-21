package com.artezio.recovery.rest.processor;

import com.artezio.recovery.rest.model.CallbackAddress;
import com.artezio.recovery.rest.model.RestRecoveryRequest;
import com.artezio.recovery.rest.repository.CallbackAddressRepository;
import com.artezio.recovery.server.data.messages.RecoveryRequest;
import com.artezio.recovery.storage.processor.RecoveryMessageProcessor;
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

import java.util.function.Function;

import static com.artezio.recovery.rest.route.RestRoute.REST_CALLBACK_ROUTE_URL;

/**
 * Recovery request processor.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Slf4j
public class RestRequestProcessor implements Processor {

    /**
     * Processor for storing recovery messages.
     */
    @Autowired
    private RecoveryMessageProcessor messageProcessor;

    /**
     * Repository for callback address.
     */
    @Autowired
    private CallbackAddressRepository repository;

    private Function<RestRecoveryRequest, RecoveryRequest> extractRecoveryRequest = restRequest -> {
        RecoveryRequest request = new RecoveryRequest();
        request.setCallbackUri(REST_CALLBACK_ROUTE_URL);
        request.setExternalId(restRequest.getExternalId());
        request.setLocker(restRequest.getLocker());
        request.setMessage(messageProcessor.processSaving(restRequest.getMessage()));
        request.setPause(restRequest.getPause());
        request.setProcessingFrom(restRequest.getProcessingFrom());
        request.setProcessingLimit(restRequest.getProcessingLimit());
        request.setProcessingTo(restRequest.getProcessingTo());
        request.setQueue(restRequest.getQueue());
        request.setQueueParent(restRequest.getQueueParent());
        return request;
    };

    @Override
    public void process(Exchange exchange) throws Exception {
        RestRecoveryRequest restRequest = exchange.getIn().getBody(RestRecoveryRequest.class);
        saveCallbackAddress(restRequest);
        exchange.getIn().setBody(extractRecoveryRequest.apply(restRequest));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.MANDATORY)
    void saveCallbackAddress(RestRecoveryRequest request) {
        CallbackAddress callbackAddress = new CallbackAddress();
        callbackAddress.setExternalId(request.getExternalId());
        callbackAddress.setCallbackUri(request.getCallbackUri());
        repository.save(callbackAddress);
        log.info("CallbackAddress for request with externalId "+ request.getExternalId() +" saved.");
    }
}
