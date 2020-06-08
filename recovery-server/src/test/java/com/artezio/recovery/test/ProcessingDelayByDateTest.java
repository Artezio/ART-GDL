/*
 */
package com.artezio.recovery.test;

import com.artezio.recovery.application.RecoveryServerApplication;
import com.artezio.recovery.server.data.messages.RecoveryOrder;
import com.artezio.recovery.server.data.messages.RecoveryRequest;
import com.artezio.recovery.server.data.access.IRecoveryOrderCrud;
import com.artezio.recovery.server.context.RecoveryRoutes;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.*;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Date;

/**
 * Processing delay by date test.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = RecoveryServerApplication.class)
@ComponentScan(
        basePackages = {
                "com.artezio.recovery.server"
        }
)
@EntityScan(
        basePackages = {
                "com.artezio.recovery.server"
        }
)
@EnableJpaRepositories(
        basePackages = {
                "com.artezio.recovery.server"
        }
)
@MockEndpoints
@Slf4j
public class ProcessingDelayByDateTest {

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
     * Timeout in milliseconds for processing delay.
     */
    private static final int DELAY_TIMEOUT = 10_000;
    /**
     * Whole recovery processing time.
     */
    private static final int ENDPOINT_TIMEOUT = 20_000;
    /**
     * Test execution timeout in milliseconds.
     */
    private static final int TEST_TIMEOUT = 60_000;
    /**
     * Apache Camel context header to fix delay checking result.
     */
    private static final String DELAY_CHECK_HEADER = "DELAY_CHECK";

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
     * Processing delay by date test definition.
     *
     * @throws Exception @see Exception
     */
    @Test(timeout = TEST_TIMEOUT)
    public void delayTest() throws Exception {
        camel.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(CALLBACK_URI)
                        .routeId("TestCallback")
                        .setExchangePattern(ExchangePattern.InOut)
                        .process((Exchange exchange) -> {
                            Date now = new Date(System.currentTimeMillis());
                            log.info(exchange.getExchangeId()
                                    + ": "
                                    + Thread.currentThread().getName());
                            RecoveryOrder order = exchange.getIn().getBody(RecoveryOrder.class);
                            exchange.getIn().setHeader(DELAY_CHECK_HEADER, now.after(order.getProcessingFrom()));
                            // Long term process emulation.
                            Thread.sleep(PRODUCER_TIMEOUT);
                        }).id("TestProcessor")
                        .to(MOCK_RESULT_URI);
            }
        });
        callback.expectedHeaderReceived(DELAY_CHECK_HEADER, Boolean.TRUE);
        RecoveryRequest req = new RecoveryRequest();
        req.setCallbackUri(CALLBACK_URI);
        req.setProcessingFrom(new Date(System.currentTimeMillis() + DELAY_TIMEOUT));
        dao.deleteAll();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setIsolationLevel(DefaultTransactionDefinition.ISOLATION_SERIALIZABLE);
        TransactionStatus status = transactionManager.getTransaction(def);
        producer.sendBody(req);
        transactionManager.commit(status);
        Thread.sleep(ENDPOINT_TIMEOUT);
        callback.assertIsSatisfied();
        camel.stop();
    }

}
