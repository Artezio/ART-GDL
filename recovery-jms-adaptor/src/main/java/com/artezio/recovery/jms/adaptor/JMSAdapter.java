package com.artezio.recovery.jms.adaptor;

import com.artezio.recovery.server.config.TransactionSupportConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;
import com.artezio.recovery.server.routes.RecoveryRoute;


@Component
@Slf4j
public class JMSAdapter extends SpringRouteBuilder  {

    /**
     * JMS Point to Point Route ID.
     */
    public static final String JMS_QUEUE_P2P_ROUTE_ID = "queue:p2p_recovery";
    /**
     * JMS Point to Point Route URL.
     */
    public static final String JMS_QUEUE_ROUTE_URL = "jms:" + JMS_QUEUE_P2P_ROUTE_ID;

    @Override
    public void configure() throws Exception {
        from(JMS_QUEUE_ROUTE_URL).transacted(TransactionSupportConfig.PROPAGATIONTYPE_PROPAGATION_REQUIRED)
                .routeId(JMS_QUEUE_P2P_ROUTE_ID)
                .to(RecoveryRoute.INCOME_URL);
    }
}