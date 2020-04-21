package com.artezio.recovery.rest.processor;

import com.artezio.recovery.rest.model.RestClientResponse;
import com.artezio.recovery.server.data.messages.ClientResponse;
import com.artezio.recovery.server.data.types.RecoveryException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Recovery response processor.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Slf4j
public class RestResponseProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        RestClientResponse restClientResponse;
        Object body = exchange.getIn().getBody();
        if (body instanceof byte[]) {
            restClientResponse = new ObjectMapper().readValue((byte[]) body, RestClientResponse.class);
        } else if (body instanceof RestClientResponse) {
            restClientResponse = (RestClientResponse) body;
        } else if (body instanceof ClientResponse) {
            return;
        } else {
            throw new RecoveryException("Wrong type of client response");
        }
        ClientResponse response = new ClientResponse();
        BeanUtils.copyProperties(response, restClientResponse);
        exchange.getIn().setBody(response);
    }

}
