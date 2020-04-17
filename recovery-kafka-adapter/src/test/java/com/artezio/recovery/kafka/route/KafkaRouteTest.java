package com.artezio.recovery.kafka.route;

import com.artezio.recovery.application.RecoveryServerApplication;
import com.artezio.recovery.kafka.application.RecoveryKafkaAdaptorApplication;
import com.artezio.recovery.kafka.model.KafkaClientResponse;
import com.artezio.recovery.kafka.model.KafkaRecoveryOrder;
import com.artezio.recovery.kafka.model.KafkaRecoveryRequest;
import com.artezio.recovery.server.data.access.IRecoveryOrderCrud;
import com.artezio.recovery.server.data.types.ClientResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Kafka route test.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = {RecoveryServerApplication.class, RecoveryKafkaAdaptorApplication.class})
@MockEndpoints
@Slf4j
//Works only with started kafka server
@Ignore
public class KafkaRouteTest {

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

    /**
     * Recovery request income route producer.
     */
    @Produce
    private ProducerTemplate producer;

    /**
     * Kafka input queue property.
     */
    @Value("${kafka.input.queue:kafka:recovery}")
    private String kafkaInputQueue;

    /**
     * Kafka input brokers property.
     */
    @Value("${kafka.input.brokers:localhost:9092}")
    private String kafkaInputBrokers;

    /**
     * Kafka output queue property.
     */
    @Value("${kafka.output.queue:kafka:callback_recovery}")
    private String kafkaOutputQueue;

    /**
     * Kafka output brokers property.
     */
    @Value("${kafka.output.brokers:localhost:9092}")
    private String kafkaOutputBrokers;

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
    public void kafkaRouteTest() throws Exception {
        camel.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(kafkaOutputQueue + "?brokers=" + kafkaOutputBrokers).routeId("TestKafkaCallbackRoute")
                        .unmarshal().json(JsonLibrary.Jackson, KafkaRecoveryOrder.class)
                        .to(CALLBACK_URI);
                from(CALLBACK_URI)
                        .routeId("TestCallback")
                        .setExchangePattern(ExchangePattern.InOut)
                        .process((Exchange exchange) -> {
                            log.info(exchange.getExchangeId()
                                    + ": "
                                    + Thread.currentThread().getName());
                            KafkaRecoveryOrder order = exchange.getIn().getBody(KafkaRecoveryOrder.class);
                            log.info("Order message: " + order.getMessage());

                            // Long term process emulation.
                            Thread.sleep(PRODUCER_TIMEOUT);
                            KafkaClientResponse response = new KafkaClientResponse();
                            response.setDescription("Test Description");
                            response.setResult(ClientResultEnum.SUCCESS);
                            exchange.getIn().setBody(response);
                        }).id("TestProcessor")
                        .to(MOCK_RESULT_URI);
            }
        });

        callback.expectedMessageCount(1);

        KafkaRecoveryRequest kafkaRequest = new KafkaRecoveryRequest();
        kafkaRequest.setMessage("Hello from Kafka Producer!");
        repository.deleteAll();

        producer.sendBody(kafkaInputQueue + "?brokers=" + kafkaInputBrokers, kafkaRequest);

        Thread.sleep(ENDPOINT_TIMEOUT);
        callback.assertIsSatisfied();
        camel.stop();
    }
}
