package com.artezio.recovery.jms.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan(
        basePackages = {
                "com.artezio.recovery"
        }
)
@EntityScan(
        basePackages = {
                "com.artezio.recovery"
        }
)
@EnableJpaRepositories(
        basePackages = {
                "com.artezio.recovery"
        }
)
@SpringBootApplication
public class RecoveryJMSAdaptorApplication {
    public static void main(String[] args) {
        new SpringApplication(RecoveryJMSAdaptorApplication.class).run(args);
    }
}