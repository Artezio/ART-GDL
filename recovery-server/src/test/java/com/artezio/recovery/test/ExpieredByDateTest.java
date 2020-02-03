/*
 */
package com.artezio.recovery.test;

import com.artezio.recovery.application.RecoveryServerApplication;
import com.artezio.recovery.server.context.RecoveryRoutes;
import com.artezio.recovery.server.data.access.IRecoveryOrderCrud;
import com.artezio.recovery.server.data.messages.ClientResponse;
import com.artezio.recovery.server.data.messages.RecoveryOrder;
import com.artezio.recovery.server.data.messages.RecoveryRequest;
import com.artezio.recovery.server.data.types.ClientResultEnum;
import com.artezio.recovery.server.data.types.ProcessingCodeEnum;
import java.util.Date;
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
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Expired by date test.
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
public class ExpieredByDateTest {

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
    private static final int DELAY_TIMEOUT = 15_000;
    /**
     * Whole recovery processing time.
     */
    private static final int ENDPOINT_TIMEOUT = 30_000;
    /**
     * Test execution timeout in milliseconds.
     */
    private static final int TEST_TIMEOUT = 60_000;
    /**
     * Apache Camel context header to check expired code.
     */
    private static final String EXPIRED_CHECK_HEADER = "EXPIRED_CHECK";

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
     * Expired by date test definition.
     *
     * @throws Exception @see Exception
     */
    @Test(timeout = TEST_TIMEOUT)
    public void expiredTest() throws Exception {
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
                            ClientResponse response = new ClientResponse();
                            if (ProcessingCodeEnum.EXPIRED_BY_DATE.equals(order.getCode())) {
                                exchange.getIn().setHeader(EXPIRED_CHECK_HEADER, Boolean.TRUE);
                                response.setResult(ClientResultEnum.BUSINESS_FATAL_ERROR);
                            } else {
                                exchange.getIn().setHeader(EXPIRED_CHECK_HEADER, Boolean.FALSE);
                                response.setResult(ClientResultEnum.BUSINESS_ERROR);
                            }
                            exchange.getIn().setBody(response);
                            // Long term process emulation.
                            Thread.sleep(PRODUCER_TIMEOUT);
                        }).id("TestProcessor")
                        .to(MOCK_RESULT_URI);
            }
        });
        callback.expectedHeaderValuesReceivedInAnyOrder(EXPIRED_CHECK_HEADER,
                Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        RecoveryRequest req = new RecoveryRequest();
        req.setCallbackUri(CALLBACK_URI);
        req.setProcessingTo(new Date(System.currentTimeMillis() + DELAY_TIMEOUT));
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
