package com.artezio.recovery.jms.adaptor;

import com.artezio.recovery.jms.config.JMSTransactionSupportConfig;
import com.artezio.recovery.jms.model.JMSRecoveryOrder;
import com.artezio.recovery.jms.processor.JMSRecoveryProcessor;
import com.artezio.recovery.server.context.RecoveryRoutes;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Recovery Apache Camel route class for JMS adaptor.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Component
@Slf4j
public class JMSRoute extends SpringRouteBuilder {

    /**
     * JMS Point to Point Route ID.
     */
    public static final String JMS_ROUTE_ID = "jmsRoute";


    /**
     * JMS callback Route ID.
     */
    public static final String JMS_CALLBACK_ROUTE_ID = "jmsCallbackRoute";

    /**
     * JMS callback route URL.
     */
    public static final String JMS_CALLBACK_ROUTE_URL = "direct://" + JMS_CALLBACK_ROUTE_ID;

    /**
     * JMS input queue URL.
     */
    @Value("${jms.input.queue:jms:p2p_recovery}")
    private String inputQueueURL;
    /**
     * JMS output queue URL.
     */
    @Value("${jms.output.queue:jms:callback_recovery}")
    private String outputQueueURL;

    /**
     * Processor for extract recoveryRequest.
     */
    @Autowired
    private JMSRecoveryProcessor requestProcessor;

    @Override
    public void configure() throws Exception {
        from(inputQueueURL).routeId(JMS_ROUTE_ID)
                .transacted(JMSTransactionSupportConfig.PROPAGATIONTYPE_PROPAGATION_REQUIRED)
                .process(requestProcessor).id(JMSRecoveryProcessor.class.getSimpleName())
                .to("log:com.artezio.recovery.jms?level=DEBUG")
                .to(RecoveryRoutes.INCOME_URL);

        from(JMS_CALLBACK_ROUTE_URL).routeId(JMS_CALLBACK_ROUTE_ID)
                .convertBodyTo(JMSRecoveryOrder.class)
                .toD(outputQueueURL);
    }
}
