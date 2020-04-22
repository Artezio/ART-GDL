package com.artezio.recovery.jms.processor;

import com.artezio.recovery.jms.model.JMSClientResponse;
import com.artezio.recovery.server.data.messages.ClientResponse;
import com.artezio.recovery.server.data.types.RecoveryException;
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
public class JMSResponseProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        ClientResponse response = new ClientResponse();
        Object body = exchange.getIn().getBody();
        if (body instanceof JMSClientResponse) {
            BeanUtils.copyProperties(response, body);
            exchange.getIn().setBody(response);
        } else if (body instanceof ClientResponse) {
            return;
        } else {
            throw new RecoveryException("Wrong type of client response");
        }
    }
}
