package com.artezio.recovery.kafka.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Recovery server application for kafka adapter.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Configuration
@ComponentScan(
        basePackages = {
                "com.artezio.recovery",
        }
)
@EntityScan(
        basePackages = {
                "com.artezio.recovery",
        }
)
@EnableJpaRepositories(
        basePackages = {
                "com.artezio.recovery",
        }
)
@SpringBootApplication
public class RecoveryKafkaAdaptorApplication {

    /**
     * Application entry point method.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        new SpringApplication(RecoveryKafkaAdaptorApplication.class).run(args);
    }
}
