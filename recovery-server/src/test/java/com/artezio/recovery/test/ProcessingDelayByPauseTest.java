/*
 */
package com.artezio.recovery.test;

import com.artezio.recovery.application.RecoveryServerApplication;
import com.artezio.recovery.server.data.model.ClientResponse;
import com.artezio.recovery.server.data.model.RecoveryOrder;
import com.artezio.recovery.server.data.model.RecoveryRequest;
import com.artezio.recovery.server.data.repository.RecoveryOrderRepository;
import com.artezio.recovery.server.data.types.ClientResultEnum;
import com.artezio.recovery.server.routes.RecoveryRoute;
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
 * Processing delay by pause rule test.
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
public class ProcessingDelayByPauseTest {

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
    private static final int ENDPOINT_TIMEOUT = 90_000;
    /**
     * Test execution timeout in milliseconds.
     */
    private static final int TEST_TIMEOUT = 120_000;
    /**
     * Apache Camel context header to fix delay checking result.
     */
    private static final String DELAY_CHECK_HEADER = "DELAY_CHECK";
    /**
     * Minimum seconds between 1 and 5 try.
     */
    private static final int PAUSE_FROM_1_TO_5_SEC = 5;
    /**
     * Minimum seconds between 5 and 7 try.
     */
    private static final int PAUSE_FROM_5_TO_7_SEC = 10;
    /**
     * Minimum seconds between each try after 7 one.
     */
    private static final int PAUSE_FROM_7 = 5;
    /**
     * Delay pause rule for recovery processing.
     */
    private static final String PAUSE_RULE = "1:" + PAUSE_FROM_1_TO_5_SEC
            + "; 5:" + PAUSE_FROM_5_TO_7_SEC
            + "; 7:" + PAUSE_FROM_7;

    /**
     * Current Apache Camel context.
     */
    @Autowired
    private CamelContext camel;
    /**
     * Data access object.
     */
    @Autowired
    private RecoveryOrderRepository dao;

    /**
     * Recovery request income route producer.
     */
    @Produce(uri = RecoveryRoute.INCOME_URL)
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
     * Processing delay by pause rule test definition.
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
                            RecoveryOrder order = exchange.getIn().getBody(RecoveryOrder.class);
                            ClientResponse response = new ClientResponse();
                            int count = order.getProcessingCount();
                            response.setResult((count > 7)
                                    ? ClientResultEnum.SUCCESS
                                    : ClientResultEnum.BUSINESS_ERROR);
                            long delayMs;
                            if (count >= 7) {
                                delayMs = PAUSE_FROM_7 * 1000;
                            } else if (count >= 5) {
                                delayMs = PAUSE_FROM_5_TO_7_SEC * 1000;
                            } else {
                                delayMs = PAUSE_FROM_1_TO_5_SEC * 1000;
                            }
                            Date modified = order.getOrderModified();
                            Date delayed = new Date(modified.getTime() + delayMs);
                            Boolean delayCheck = now.after(delayed);
                            exchange.getIn().setHeader(DELAY_CHECK_HEADER, delayCheck);
                            exchange.getIn().setBody(response);
                            log.info(Thread.currentThread().getName()
                                    + "; count = " + count
                                    + "; delayCheck = " + delayCheck
                                    + "; modified = " + modified
                                    + "; delayed = " + delayed
                                    + "; now = " + now);
                            // Long term process emulation.
                            Thread.sleep(PRODUCER_TIMEOUT);
                        }).id("TestProcessor")
                        .to(MOCK_RESULT_URI);
            }
        });
        callback.expectedHeaderValuesReceivedInAnyOrder(DELAY_CHECK_HEADER,
                Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE,
                Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);
        RecoveryRequest req = new RecoveryRequest();
        req.setCallbackUri(CALLBACK_URI);
        req.setPause(PAUSE_RULE);
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
