package com.artezio.recovery.processor;


import com.artezio.recovery.model.RecoveryRequestDTO;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Recovery request unwrapping processor.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class UnwrappingProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        if (exchange.getIn().getBody() instanceof RecoveryRequestDTO) {
            RecoveryRequestDTO request = (RecoveryRequestDTO) exchange.getIn().getBody();
            exchange.getIn().setBody(request.getRecoveryRequest());
        }
    }
}
