package com.artezio.recovery.server.adapters;

import com.artezio.recovery.server.context.RecoveryRoutes;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class JMSAdapter extends SpringRouteBuilder {

    /**
     * Timer route ID.
     */
    public static final String JMS_QUEUE_P2P_ROUTE_ID = "queue:p2p_recovery";
    /**
     * Timer route URL.
     */
    public static final String JMS_QUEUE_ROUTE_URL = "jms:" + JMS_QUEUE_P2P_ROUTE_ID;

    @Override
    public void configure() throws Exception {
        from(JMS_QUEUE_ROUTE_URL).transacted("PROPAGATION_REQUIRED")
                .routeId(JMS_QUEUE_P2P_ROUTE_ID)
                .to(RecoveryRoutes.INCOME_URL);
    }
}
