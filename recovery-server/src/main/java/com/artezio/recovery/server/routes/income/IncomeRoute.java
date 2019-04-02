/*
 */
package com.artezio.recovery.server.routes.income;

import com.artezio.recovery.server.processors.CallbackProcessor;
import com.artezio.recovery.server.processors.StoringProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Recovery income Apache Camel route class.
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Component
@Slf4j
public class IncomeRoute extends SpringRouteBuilder {

    /**
     * Income route ID.
     */
    public static final String ID = "IncomeRoute";
    /**
     * Income route URL.
     */
    public static final String URL = "direct://" + ID;
    /**
     * Income route URI.
     */
    private static final String URI = URL;
    /**
     * Recovery request storing processor.
     */
    @Autowired
    private StoringProcessor storing;
    /**
     * Recovery callback processor.
     */
    @Autowired
    private CallbackProcessor callback;
    
    /**
     * Income recovery Apache Camel route definition.
     * 
     * @throws Exception @see Exception
     */
    @Override
    public void configure() throws Exception {
        log.debug(URI);
        from(URI)
                .routeId(ID)
                .process(storing)
                .process(callback);
    }
    
}
