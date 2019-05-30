/*
 */
package com.artezio.example.billling.adaptor.web;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Web user session bean.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@SessionScope
@ManagedBean
@Slf4j
public class UserSession {
    
    @Getter
    private String testMessage = "Hello world!";

    @PostConstruct
    public void createSession() {
        log.info("User session created.");
    }
}
