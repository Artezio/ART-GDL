/*
 */
package com.artezio.recovery.test;

import com.artezio.recovery.server.RecoveryServerApplication;
import com.artezio.recovery.server.context.RecoveryRoutes;
import com.artezio.recovery.server.data.messages.RecoveryRequest;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = RecoveryServerApplication.class)
@MockEndpoints
@Slf4j
public class MassLoadingSimpleTest {

    private static final String CALLBACK_URI = "direct://callback";
    private static final String MOCK_RESULT_URI = "mock:direct:callback";
    private static final int TEST_TIMEOUT = 60_000;
    private static final int TEST_LOAD_NUMBER = 2_000;

    @Autowired
    private ProducerTemplate producer;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @EndpointInject(uri = MOCK_RESULT_URI)
    private MockEndpoint callback;

    @Test(timeout = TEST_TIMEOUT)
    public void massLoad() throws InterruptedException {
        callback.whenAnyExchangeReceived((Exchange exchange) -> {
            log.info(exchange.getExchangeId());
            Thread.sleep(5_000);
        });
        callback.expectedMessageCount(TEST_LOAD_NUMBER);
        for (int i = 0; i < TEST_LOAD_NUMBER; i++) {
            RecoveryRequest req = new RecoveryRequest();
            req.setCallbackUri(CALLBACK_URI);
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setIsolationLevel(DefaultTransactionDefinition.ISOLATION_SERIALIZABLE);
            TransactionStatus status = transactionManager.getTransaction(def);
            producer.requestBody(RecoveryRoutes.INCOME_URL, req);
            transactionManager.commit(status);
        }
        callback.await(TEST_TIMEOUT / 2, TimeUnit.MILLISECONDS);
        callback.assertIsSatisfied();
    }

}
