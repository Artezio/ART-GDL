package com.artezio.recovery.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Recovery server application for rest adapter.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@SpringBootApplication
public class RestAdapterApplication {

    /**
     * Application entry point method.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        new SpringApplication(RestAdapterApplication.class).run(args);
    }

}
