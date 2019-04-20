/*
 */
package com.artezio.recovery.test;

import com.artezio.recovery.server.RecoveryServerApplication;
import com.artezio.recovery.server.context.RecoveryRoutes;
import com.artezio.recovery.server.data.messages.RecoveryRequest;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Mass loading test for simple recovery requests.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(
        classes = RecoveryServerApplication.class,
        properties = {
            "com.artezio.recovery.seda.consumers=20",
            "spring.datasource.hikari.maximumPoolSize=20"
        }
)
@MockEndpoints
@Slf4j
public class MassLoadingSimpleTest {

    /**
     * Test callback route URI.
     */
    private static final String CALLBACK_URI = "direct://callback";
    /**
     * Test callback mock endpoint URI.
     */
    private static final String MOCK_RESULT_URI = "mock:callback";
    /**
     * Timeout in milliseconds to emulate long term remote execution.
     */
    private static final int PRODUCER_TIMEOUT = 5_000;
    /**
     * Whole recovery processing time.
     */
    private static final int ENDPOINT_TIMEOUT = 60_000;
    /**
     * Test execution timeout in milliseconds.
     */
    private static final int TEST_TIMEOUT = 300_000;
    /**
     * Amount of SEDA concurrent consumers property.
     */
    @Value("${com.artezio.recovery.seda.consumers}")
    private int sedaConsumers;

    /**
     * Current Apache Camel context.
     */
    @Autowired
    private CamelContext camel;

    /**
     * Recovery request income route producer.
     */
    @Produce(uri = RecoveryRoutes.INCOME_URL)
    private ProducerTemplate producer;

    /**
     * Spring transaction manager.
     */
    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * Test callback mock endpoint.
     */
    @EndpointInject(uri = MOCK_RESULT_URI)
    private MockEndpoint callback;
    
    /**
     * Mass loading test for simple recovery requests definition.
     *
     * @throws Exception @see Exception
     */
    @Test(timeout = TEST_TIMEOUT)
    public void massLoad() throws Exception {
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
                            // Long term process emulation.
                            Thread.sleep(PRODUCER_TIMEOUT);
                        }).id("TestProcessor")
                        .to(MOCK_RESULT_URI);
            }
        });
        final int loadNumber = (ENDPOINT_TIMEOUT / PRODUCER_TIMEOUT) * sedaConsumers;
        callback.expectedMessageCount(loadNumber);
        for (int i = 0; i < loadNumber; i++) {
            RecoveryRequest req = new RecoveryRequest();
            req.setCallbackUri(CALLBACK_URI);
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setIsolationLevel(DefaultTransactionDefinition.ISOLATION_SERIALIZABLE);
            TransactionStatus status = transactionManager.getTransaction(def);
            producer.sendBody(req);
            transactionManager.commit(status);
        }
        Thread.sleep(ENDPOINT_TIMEOUT);
        callback.assertIsSatisfied();
        camel.stop();
    }

}
