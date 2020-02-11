package com.artezio.recovery.route;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.artezio.recovery.application.RecoveryServerApplication;
import com.artezio.recovery.application.RestAdapterApplication;
import com.artezio.recovery.server.data.model.RecoveryOrder;
import com.artezio.recovery.server.data.model.RecoveryRequest;
import com.artezio.recovery.server.data.repository.RecoveryOrderRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Rest route test.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = {RecoveryServerApplication.class, RestAdapterApplication.class})
@ComponentScan(
    basePackages = {
        "com.artezio.recovery"
    }
)
@EntityScan(
    basePackages = {
        "com.artezio.recovery"
    }
)
@EnableJpaRepositories(
    basePackages = {
        "com.artezio.recovery"
    }
)
@MockEndpoints
@Slf4j
public class RestRouteTest {

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
    private RecoveryOrderRepository repository;

    /**
     * Recovery request income route producer.
     */
    @Produce(uri = "rest:post:recover")
    private ProducerTemplate producer;

    /**
     * Test callback mock endpoint.
     */
    @EndpointInject(uri = MOCK_RESULT_URI)
    private MockEndpoint callback;

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
    public void restRouteTest() throws Exception {
        camel.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(CALLBACK_URI)
                    .routeId("TestCallback")
                    .setExchangePattern(ExchangePattern.InOut)
                    .process((Exchange exchange) -> {
                        log.info(exchange.getExchangeId()
                            + ": "
                            + Thread.currentThread().getName());
                        RecoveryOrder order = exchange.getIn().getBody(RecoveryOrder.class);
                        log.info("Order message: " + order.getMessage());

                        // Long term process emulation.
                        Thread.sleep(PRODUCER_TIMEOUT);
                    }).id("TestProcessor")
                    .to(MOCK_RESULT_URI);
            }
        });

        callback.expectedMessageCount(1);

        RecoveryRequest req = new RecoveryRequest();
        req.setCallbackUri(CALLBACK_URI);
        req.setMessage("Hello from Rest Producer!");
        repository.deleteAll();

        producer.sendBody(req);

        Thread.sleep(ENDPOINT_TIMEOUT);
        callback.assertIsSatisfied();
        camel.stop();
    }
}
