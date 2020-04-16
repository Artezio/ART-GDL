package com.artezio.recovery.rest.processor;

import com.artezio.recovery.rest.model.CallbackAddress;
import com.artezio.recovery.rest.repository.CallbackAddressRepository;
import com.artezio.recovery.server.data.messages.RecoveryOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Rest callback processor.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Slf4j
public class RestCallbackProcessor implements Processor {

    /**
     * Repository fro callback address.
     */
    @Autowired
    private CallbackAddressRepository repository;

    @Override
    public void process(Exchange exchange) throws Exception {
        RecoveryOrder recoveryOrder = exchange.getIn().getBody(RecoveryOrder.class);
        CallbackAddress callbackAddress = repository.findByExternalId(recoveryOrder.getExternalId());
        if (callbackAddress != null && callbackAddress.getCallbackUri() != null && !callbackAddress.getCallbackUri().isEmpty()) {
            log.info("CallbackAddress for request with externalId "+ recoveryOrder.getExternalId() +" received.");
            exchange.getIn().setHeader("callbackUri", callbackAddress.getCallbackUri());
        }
    }
}
