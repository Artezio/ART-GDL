package com.artezio.recovery.jms.adaptor;

import com.artezio.recovery.processor.UnwrappingProcessor;
import com.artezio.recovery.server.config.TransactionSupportConfig;
import com.artezio.recovery.server.routes.RecoveryRoute;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class JMSRoute extends SpringRouteBuilder {

    /**
     * JMS Point to Point Route ID.
     */
    public static final String JMS_ROUTE_ID = "JmsQueueRoute";

    /**
     * JMS input queue URL.
     */
    @Value("${input.queue.name:jms:p2p_recovery}")
    private String inputQueueURL;

    /**
     * Processor for unwrapping from DTO.
     */
    @Autowired
    private UnwrappingProcessor unwrapping;

    @Override
    public void configure() throws Exception {
        from(inputQueueURL).transacted(TransactionSupportConfig.PROPAGATIONTYPE_PROPAGATION_REQUIRED)
                .routeId(JMS_ROUTE_ID)
                .process(unwrapping).id(UnwrappingProcessor.class.getSimpleName())
                .to("log:com.artezio.recovery.jms?level=DEBUG")
                .to(RecoveryRoute.INCOME_URL);
    }
}
