package com.artezio.recovery.jms.route;

import com.artezio.recovery.jms.application.RecoveryJMSAdaptorApplication;
import com.artezio.recovery.jms.model.JMSClientResponse;
import com.artezio.recovery.jms.model.JMSRecoveryOrder;
import com.artezio.recovery.jms.model.JMSRecoveryRequest;
import com.artezio.recovery.server.data.access.IRecoveryOrderCrud;
import com.artezio.recovery.server.data.types.ClientResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = RecoveryJMSAdaptorApplication.class)
@MockEndpoints
@Slf4j
@Transactional
public class JMSDeliveryTest {

    /**
     * Test callback route URI.
     */
    private static final String CALLBACK_URI = "direct://callback";
    /**
     * Test callback mock endpoint URI.
     */
    private static final String MOCK_RESULT_URI = "mock:callback";

    /**
     * Current Apache Camel context.
     */
    @Autowired
    private CamelContext camel;

    /**
     * Data access object.
     */
    @Autowired
    private IRecoveryOrderCrud dao;

    /**
     * Recovery request income route producer.
     */
    @Produce
    private ProducerTemplate producer;

    /**
     * Test callback mock endpoint.
     */
    @EndpointInject(uri = MOCK_RESULT_URI)
    private MockEndpoint callback;

    /**
     * Priority property.
     */
    @Value("${camel.component.jms.priority}")
    private int priority;

    /**
     * Receive timeout property.
     */
    @Value("${camel.component.jms.receive-timeout}")
    private long receiveTimeout;

    /**
     * Request timeout property.
     */
    @Value("${camel.component.jms.request-timeout}")
    private long requestTimeout;

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
     * Timeout in milliseconds to emulate long term remote execution.
     */
    private static final int PRODUCER_TIMEOUT = 5_000;

    /**
     * Test execution timeout in milliseconds.
     */
    private static final int TEST_TIMEOUT = 60_000;

    /**
     * Whole recovery processing time.
     */
    private static final int ENDPOINT_TIMEOUT = 30_000;


    @Test(timeout = TEST_TIMEOUT)
    public void jmsTest() throws Exception {
        camel.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(outputQueueURL).routeId("TestJmsCallbackRoute")
                        .to(CALLBACK_URI);
                from(CALLBACK_URI)
                        .routeId("TestCallback")
                        .setExchangePattern(ExchangePattern.InOut)
                        .process((Exchange exchange) -> {
                            log.info(exchange.getExchangeId()
                                    + ": "
                                    + Thread.currentThread().getName());
                            JMSRecoveryOrder order = exchange.getIn().getBody(JMSRecoveryOrder.class);
                            log.info("Order message: " + order.getMessage());

                            // Long term process emulation.
                            Thread.sleep(PRODUCER_TIMEOUT);
                            JMSClientResponse response = new JMSClientResponse();
                            response.setDescription("Test Description");
                            response.setResult(ClientResultEnum.SUCCESS);
                            exchange.getIn().setBody(response);
                        }).id("TestProcessor")
                        .to(MOCK_RESULT_URI);
            }
        });

        callback.expectedMessageCount(1);

        JMSRecoveryRequest request = new JMSRecoveryRequest();
        request.setMessage("Hello from JMS Producer!");
        dao.deleteAll();
        producer.sendBody(inputQueueURL, request);

        Thread.sleep(ENDPOINT_TIMEOUT);
        callback.assertIsSatisfied();
        Assert.assertEquals(priority, camel.getComponent("jms", JmsComponent.class).getConfiguration().getPriority());
        Assert.assertEquals(receiveTimeout, camel.getComponent("jms", JmsComponent.class).getConfiguration().getReceiveTimeout());
        Assert.assertEquals(requestTimeout, camel.getComponent("jms", JmsComponent.class).getConfiguration().getRequestTimeout());
        camel.stop();

    }

}
