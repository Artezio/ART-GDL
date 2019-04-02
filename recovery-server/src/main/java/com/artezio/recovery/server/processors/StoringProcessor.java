/*
 */
package com.artezio.recovery.server.processors;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

/**
 * Recovery request storing processor.
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Component
@Slf4j
public class StoringProcessor implements Processor {

    /**
     * Recovery request storing process definition.
     * 
     * @param exchange Apache Camel ESB exchange message.
     * @throws Exception @see Exception
     */
    @Override
    public void process(Exchange exchange) throws Exception {
    }
    
}
