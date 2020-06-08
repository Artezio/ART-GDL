package com.artezio.recovery.kafka.route;

import com.artezio.recovery.kafka.config.KafkaTransactionSupportConfig;
import com.artezio.recovery.kafka.model.KafkaRecoveryOrder;
import com.artezio.recovery.kafka.processor.KafkaRequestProcessor;
import com.artezio.recovery.kafka.processor.KafkaResponseProcessor;
import com.artezio.recovery.server.context.RecoveryRoutes;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Recovery Apache Camel kafka route class.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Component
@Slf4j
public class KafkaRoute extends SpringRouteBuilder {

    /**
     * Kafka route ID.
     */
    private static final String KAFKA_ROUTE_ID = "kafkaRoute";

    /**
     * Kafka callback Route ID.
     */
    public static final String KAFKA_CALLBACK_ROUTE_ID = "kafkaCallbackRoute";

    /**
     * Kafka callback route URL.
     */
    public static final String KAFKA_CALLBACK_ROUTE_URL = "direct://" + KAFKA_CALLBACK_ROUTE_ID;

    /**
     * Kafka input queue property.
     */
    @Value("${kafka.input.queue:kafka:recovery}")
    private String inputQueueURL;

    /**
     * Kafka input brokers property.
     */
    @Value("${kafka.input.brokers:localhost:9092}")
    private String kafkaBrokers;

    /**
     * Kafka output queue property.
     */
    @Value("${kafka.output.queue:kafka:callback_recovery}")
    private String outputQueueURL;


    /**
     * Kafka output brokers property.
     */
    @Value("${kafka.output.brokers:localhost:9092}")
    private String outputBrokers;

    /**
     * Processor for extract recoveryRequest.
     */
    @Autowired
    private KafkaRequestProcessor requestProcessor;

    /**
     * Processor for handling client response.
     */
    @Autowired
    private KafkaResponseProcessor responseProcessor;

    @Override
    public void configure() throws Exception {
        from(inputQueueURL + "?brokers=" + kafkaBrokers).id(KAFKA_ROUTE_ID)
                .transacted(KafkaTransactionSupportConfig.PROPAGATIONTYPE_PROPAGATION_REQUIRED)
                .process(requestProcessor).id(KafkaRequestProcessor.class.getSimpleName())
                .to("log:com.artezio.recovery.kafka?level=DEBUG")
                .to(RecoveryRoutes.INCOME_URL);

        from(KAFKA_CALLBACK_ROUTE_URL).id(KAFKA_CALLBACK_ROUTE_ID)
                .transacted(KafkaTransactionSupportConfig.PROPAGATIONTYPE_PROPAGATION_REQUIRED)
                .convertBodyTo(KafkaRecoveryOrder.class)
                .toD(outputQueueURL + "?brokers=" + outputBrokers)
                .process(responseProcessor).id(KafkaResponseProcessor.class.getSimpleName());
    }
}
