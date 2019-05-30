/*
 */
package com.artezio.example.billling.adaptor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Billing adaptor example application entry point class.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Configuration
@ComponentScan(
        basePackages = {
            "com.artezio.recovery.server",
            "com.artezio.example.billling.adaptor"
        }
)
@EntityScan(
        basePackages = {
            "com.artezio.recovery.server",
            "com.artezio.example.billling.adaptor"
        }
)
@EnableJpaRepositories(
        basePackages = {
            "com.artezio.recovery.server",
            "com.artezio.example.billling.adaptor"
        }
)
@SpringBootApplication
public class ExampleServerApplication {

    /**
     * Application entry point method.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        new SpringApplication(ExampleServerApplication.class).run(args);
    }

}
