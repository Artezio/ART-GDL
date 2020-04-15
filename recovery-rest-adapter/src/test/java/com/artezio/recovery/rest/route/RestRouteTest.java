package com.artezio.recovery.rest.route;

import com.artezio.recovery.model.ClientResponseDTO;
import com.artezio.recovery.model.RecoveryOrderDTO;
import com.artezio.recovery.rest.application.RecoveryRestAdaptorApplication;
import com.artezio.recovery.rest.model.RestRecoveryRequest;
import com.artezio.recovery.rest.repository.CallbackAddressRepository;
import com.artezio.recovery.server.data.messages.RecoveryOrder;
import com.artezio.recovery.server.data.access.IRecoveryOrderCrud;
import com.artezio.recovery.server.data.types.ClientResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jetty9.JettyHttpComponent9;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Rest route test.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = {RecoveryRestAdaptorApplication.class})
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
    private IRecoveryOrderCrud repository;

    @Autowired
    private CallbackAddressRepository callbackAddressRepository;

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
     * Minimum number of threads in server thread pool.
     */
    @Value("${camel.component.jetty.min-threads}")
    private Integer minThreads;

    /**
     * Maximum number of threads in server thread pool.
     */
    @Value("${camel.component.jetty.max-threads}")
    private Integer maxThreads;

    /**
     * Connection timeout property.
     */
    @Value("${camel.component.jetty.continuation-timeout}")
    private Long timeout;

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
                rest()
                        .post("/callback").id("TestCallbackRoute")
                        .consumes("application/json")
                        .type(RecoveryOrder.class)
                        .to(CALLBACK_URI);
                from(CALLBACK_URI)
                        .routeId("TestCallback")
                        .setExchangePattern(ExchangePattern.InOut)
                        .process((Exchange exchange) -> {
                            log.info(exchange.getExchangeId()
                                    + ": "
                                    + Thread.currentThread().getName());
                            RecoveryOrderDTO order = exchange.getIn().getBody(RecoveryOrderDTO.class);
                            log.info("Order message: " + order.getMessage());

                            // Long term process emulation.
                            Thread.sleep(PRODUCER_TIMEOUT);
                            ClientResponseDTO responseDTO = new ClientResponseDTO();
                            responseDTO.setDescription("Test Description");
                            responseDTO.setResult(ClientResultEnum.SUCCESS);
                            exchange.getIn().setBody(responseDTO.getResponse());
                        }).id("TestProcessor")
                        .to(MOCK_RESULT_URI);
            }
        });
        callback.expectedMessageCount(1);

        RestRecoveryRequest request = new RestRecoveryRequest();
        request.setCallbackUri("rest:post:callback?host=localhost:8080");
        request.setMessage("Hello from Rest Producer!");
        request.setExternalId("123");
        repository.deleteAll();
        callbackAddressRepository.deleteAll();

        producer.sendBody(request);

        Thread.sleep(ENDPOINT_TIMEOUT);
        callback.assertIsSatisfied();
        Assert.assertEquals(minThreads, camel.getComponent("jetty", JettyHttpComponent9.class).getMinThreads());
        Assert.assertEquals(maxThreads, camel.getComponent("jetty", JettyHttpComponent9.class).getMaxThreads());
        Assert.assertEquals(timeout, camel.getComponent("jetty", JettyHttpComponent9.class).getContinuationTimeout());
        camel.stop();
    }
}
