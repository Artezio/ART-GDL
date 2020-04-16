package com.artezio.recovery.kafka.config;

import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Transaction config for Kafka adaptor.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Configuration
public class KafkaTransactionSupportConfig {

    public static final String PROPAGATIONTYPE_PROPAGATION_REQUIRED = "PROPAGATION_REQUIRED";

    @Bean(name = PROPAGATIONTYPE_PROPAGATION_REQUIRED)
    public SpringTransactionPolicy propagationRequired(PlatformTransactionManager jtaTransactionManager) {
        SpringTransactionPolicy propagationRequired = new SpringTransactionPolicy();
        propagationRequired.setTransactionManager(jtaTransactionManager);
        propagationRequired.setPropagationBehaviorName(PROPAGATIONTYPE_PROPAGATION_REQUIRED);
        return propagationRequired;
    }
}
