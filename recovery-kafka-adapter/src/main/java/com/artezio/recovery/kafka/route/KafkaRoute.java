package com.artezio.recovery.kafka.route;

import com.artezio.recovery.model.RecoveryRequest;
import com.artezio.recovery.server.config.TransactionSupportConfig;
import com.artezio.recovery.server.routes.RecoveryRoute;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
     * Server host property.
     */
    @Value("${kafka.server.host:localhost}")
    private String serverHost;

    /**
     * Server port property.
     */
    @Value("${kafka.server.port:9092}")
    private String serverPort;

    @Override
    public void configure() throws Exception {
        from("kafka:" + topic + "?brokers=" + serverHost + ":" + serverPort)
                .id(KAFKA_ROUTE_ID)
                .transacted(TransactionSupportConfig.PROPAGATIONTYPE_PROPAGATION_REQUIRED)
                .unmarshal().json(JsonLibrary.Jackson, RecoveryRequest.class)
                .to("log:com.artezio.recovery.kafka?level=DEBUG")
                .to(RecoveryRoute.INCOME_URL);
    }
}
