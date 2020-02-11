package com.artezio.recovery.jms.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RecoveryJMSAdaptorApplication {
    public static void main(String[] args) {
        new SpringApplication(RecoveryJMSAdaptorApplication.class).run(args);
    }
}