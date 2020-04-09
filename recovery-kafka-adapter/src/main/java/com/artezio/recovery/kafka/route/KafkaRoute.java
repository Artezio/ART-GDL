package com.artezio.recovery.kafka.route;

import com.artezio.recovery.model.RecoveryRequestDTO;
import com.artezio.recovery.processor.UnwrappingProcessor;
import com.artezio.recovery.server.config.TransactionSupportConfig;
import com.artezio.recovery.server.routes.RecoveryRoute;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.dataformat.JsonLibrary;
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
     * Kafka topic property.
     */
    @Value("${kafka.topic:test}")
    private String topic;

    /**
     * Kafka brokers property.
     */
    @Value("${kafka.brokers:localhost:9092}")
    private String kafkaBrokers;

    /**
     * Processor for unwrapping from DTO.
     */
    @Autowired
    private UnwrappingProcessor unwrapping;

    @Override
    public void configure() throws Exception {
        from("kafka:" + topic + "?brokers=" + kafkaBrokers)
                .id(KAFKA_ROUTE_ID)
                .transacted(TransactionSupportConfig.PROPAGATIONTYPE_PROPAGATION_REQUIRED)
                .unmarshal().json(JsonLibrary.Jackson, RecoveryRequestDTO.class)
                .process(unwrapping).id(UnwrappingProcessor.class.getSimpleName())
                .to("log:com.artezio.recovery.kafka?level=DEBUG")
                .to(RecoveryRoute.INCOME_URL);
    }
}
