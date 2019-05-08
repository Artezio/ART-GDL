/*
 */
package com.artezio.example.billling.adaptor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Billing adaptor example application entry point class.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@SpringBootApplication(
        scanBasePackages = {
            "com.artezio.recovery.server",
            "com.artezio.example.billling.adaptor"
        }
)
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
