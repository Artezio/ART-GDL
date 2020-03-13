package com.artezio.recovery.server.config;

import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TransactionSupportConfig {

    public static final String PROPAGATIONTYPE_PROPAGATION_REQUIRED = "PROPAGATION_REQUIRED";

    @Bean(name = PROPAGATIONTYPE_PROPAGATION_REQUIRED)
    public SpringTransactionPolicy propagationRequired(PlatformTransactionManager jtaTransactionManager){
        SpringTransactionPolicy propagationRequired = new SpringTransactionPolicy();
        propagationRequired.setTransactionManager(jtaTransactionManager);
        propagationRequired.setPropagationBehaviorName(PROPAGATIONTYPE_PROPAGATION_REQUIRED);
        return propagationRequired;
    }
}
