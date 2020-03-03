package com.artezio.recovery.jms.adaptor;

import com.artezio.recovery.processor.UnwrappingProcessor;
import com.artezio.recovery.server.config.TransactionSupportConfig;
import com.artezio.recovery.server.routes.RecoveryRoute;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class JMSRoute extends SpringRouteBuilder {

    /**
     * JMS Point to Point Route ID.
     */
    public static final String JMS_QUEUE_P2P_ROUTE_ID = "queue:p2p_recovery";
    /**
     * JMS Point to Point Route URL.
     */
    public static final String JMS_QUEUE_ROUTE_URL = "jms:" + JMS_QUEUE_P2P_ROUTE_ID;

    /**
     * Processor for unwrapping from DTO.
     */
    @Autowired
    private UnwrappingProcessor unwrapping;

    @Override
    public void configure() throws Exception {
        from(JMS_QUEUE_ROUTE_URL).transacted(TransactionSupportConfig.PROPAGATIONTYPE_PROPAGATION_REQUIRED)
                .routeId(JMS_QUEUE_P2P_ROUTE_ID)
                .process(unwrapping).id(UnwrappingProcessor.class.getSimpleName())
                .to(RecoveryRoute.INCOME_URL);
    }
}
