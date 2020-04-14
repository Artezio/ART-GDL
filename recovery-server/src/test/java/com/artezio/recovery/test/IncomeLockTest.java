/*
 */
package com.artezio.recovery.test;

import com.artezio.recovery.application.RecoveryServerApplication;
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

/**
 * Same locking code income test.
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
public class IncomeLockTest {

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
    private static final int ENDPOINT_TIMEOUT = 10_000;
    /**
     * Test execution timeout in milliseconds.
     */
    private static final int TEST_TIMEOUT = 60_000;

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
     * Same locking code income test definition.
     *
     * @throws Exception @see Exception
     */
    @Test(timeout = TEST_TIMEOUT)
    public void incomeTest() throws Exception {
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
        final String lockingCode = "test-lock";
        callback.expectedMessageCount(1);
        dao.deleteAll();
        for (int i = 0; i < 5; i++) {
            RecoveryRequest req = new RecoveryRequest();
            req.setCallbackUri(CALLBACK_URI);
            req.setLocker(lockingCode);
            TransactionStatus status = null;
            try {
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setIsolationLevel(DefaultTransactionDefinition.ISOLATION_SERIALIZABLE);
                status = transactionManager.getTransaction(def);
                producer.sendBody(req);
                transactionManager.commit(status);
            } catch (CamelExecutionException e) {
                if (e.getCause() != null) {
                    log.error(e.getCause().getClass().getSimpleName()
                            + ": "
                            + e.getCause().getMessage());
                } else {
                    log.error(e.getClass().getSimpleName()
                            + ": "
                            + e.getMessage());
                }
                if (status != null) {
                    transactionManager.rollback(status);
                }
            }
        }
        Thread.sleep(ENDPOINT_TIMEOUT);
        callback.assertIsSatisfied();
        camel.stop();
    }

}
