package com.artezio.recovery.client;

import com.artezio.recovery.model.RecoveryRequest;
import com.artezio.recovery.server.routes.RecoveryRoute;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * Recovery request service class.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Service
@Slf4j
public class RecoveryRequestService {

    /**
     * REST endpoint host property.
     */
    @Value("${rest.server.host:localhost}")
    private String host;

    /**
     * REST endpoint port property.
     */
    @Value("${rest.server.port:8080}")
    private String port;

    /**
     * Access to the current Apache Camel context.
     */
    @Autowired
    private CamelContext context;

    /**
     * Recovery request income route producer.
     */
    @Produce(uri = RecoveryRoute.INCOME_URL)
    private ProducerTemplate directProducer;

    /**
     * Sends recovery request.
     *
     * @param request Recovery request that should be send.
     */
    public void sendRequest(RecoveryRequest request) {
        directProducer.sendBody(request);
    }

    /**
     * Checks if camel is started recovery request.
     *
     * @return true if camel is started.
     */
    public boolean isServerStarted() {
        return context.getStatus().isStarted();
    }

    /**
     * Stops camel and all its routes.
     */
    public void stopRoutes() throws Exception {
        for (Route route : context.getRoutes()) {
            context.stopRoute(route.getId());
        }
        context.stop();
    }

    /**
     * Stops camel and all its routes with defined timeouts for every route.
     *
     * @param timeouts the map with timeout values for all routes.
     */
    public void stopRoutes(HashMap<String, Integer> timeouts) throws Exception {
        for (Entry<String, Integer> entry : timeouts.entrySet()) {
            if (context.getRoute(entry.getKey()) != null) {
                context.stopRoute(entry.getKey(), entry.getValue(), TimeUnit.MILLISECONDS);
            }
        }
        context.stop();
    }

    /**
     * Starts camel context.
     */
    public void startContext() throws Exception {
        context.start();
    }
}
