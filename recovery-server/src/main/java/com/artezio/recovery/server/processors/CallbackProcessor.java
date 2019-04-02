/*
 */
package com.artezio.recovery.server.processors;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

/**
 * Recovery callback processor.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Component
@Slf4j
public class CallbackProcessor implements Processor {

    /**
     * Recovery callback processing definition.
     * 
     * @param exchange Apache Camel ESB exchange message.
     * @throws Exception @see Exception
     */
    @Override
    public void process(Exchange exchange) throws Exception {
        StringBuilder logMsg = new StringBuilder(exchange.getExchangeId());
        logMsg.append(" ").append(Thread.currentThread().getName());
        log.debug(logMsg.toString());
        Thread.sleep(5000);
    }

}
