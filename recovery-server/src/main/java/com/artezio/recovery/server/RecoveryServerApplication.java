/*
 */
package com.artezio.recovery.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Recovery server application entry point class.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@SpringBootApplication
public class RecoveryServerApplication {

    /**
     * Application entry point method.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        new SpringApplication(RecoveryServerApplication.class).run(args);
    }
}
