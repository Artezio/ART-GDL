package com.artezio.recovery.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.artezio.recovery.model.RecoveryRequest;
import com.artezio.recovery.server.data.exception.RecoveryException;
import com.artezio.recovery.server.data.types.DeliveryMethodType;
import com.artezio.recovery.server.routes.RecoveryRoute;

import lombok.extern.slf4j.Slf4j;

/**
 * Recovery request service class.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Service
@Slf4j
public class RecoveryRequestService {

    /**
     * JMS queue route URL.
     */
    private String JMS_QUEUE_ROUTE_URL = "jms:queue:p2p_recovery";

    /**
     * REST endpoint route URL.
     */
    private String REST_ROUTE_URL = "rest:post:recover";

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
     * Producer template.
     */
    private ProducerTemplate producer;

    /**
     * Recovery request income route producer.
     */
    @Produce(uri = RecoveryRoute.INCOME_URL)
    private ProducerTemplate directProducer;

    /**
     * Sends recovery request depending on delivery method.
     *
     * @param request Recovery request that should be send.
     * @param deliveryMethodType Delivery method type for choosing producer.
     */
    public void sendRequest(DeliveryMethodType deliveryMethodType, RecoveryRequest request)
        throws RecoveryException {
        ProducerTemplate producer = getProducer(deliveryMethodType);
        producer.sendBody(request);
    }

    /**
     * Sends recovery request.
     *
     * @param request Recovery request that should be send.
     */
    public void sendRequest(RecoveryRequest request) {
        directProducer.sendBody(request);
    }

    public boolean isServerStarted() {
        return context.getStatus().isStarted();
    }

    public void stopRoutes() throws Exception {
        List<Route> routes = context.getRoutes();
        for (Route route : routes) {
            context.stopRoute(route.getId());
        }
        context.stop();
    }

    public void stopRoutes(HashMap<String, Integer> timeouts) throws Exception {
        for (Entry<String, Integer> entry : timeouts.entrySet()) {
            if (context.getRoute(entry.getKey()) != null) {
                context.stopRoute(entry.getKey(), entry.getValue(), TimeUnit.MILLISECONDS);
            }
        }
        context.stop();
    }

    public void startContext() throws Exception {
        context.start();
    }

    /**
     * Returns producer depending on delivery type.
     *
     * @param deliveryMethodType delivery type.
     * @return producer.
     */
    private ProducerTemplate getProducer(DeliveryMethodType deliveryMethodType)
        throws RecoveryException {
        ProducerTemplate producer = getProducerTemplate();
        switch (deliveryMethodType) {
            case JMS:
                producer.setDefaultEndpointUri(JMS_QUEUE_ROUTE_URL);
                break;
            case REST:
                producer.setDefaultEndpointUri(REST_ROUTE_URL + "?host=" + host + ":" + port);
                break;
            case DIRECT:
                producer.setDefaultEndpointUri(RecoveryRoute.INCOME_URL);
                break;
            default:
                throw new RecoveryException("Wrong delivery type");
        }
        log.info("Default Endpoint URI: " + producer.getDefaultEndpoint().getEndpointUri());
        return producer;
    }

    /**
     * Returns producer if not null or request the new one from camel context.
     *
     * @return producer.
     */
    private ProducerTemplate getProducerTemplate() {
        return Optional.ofNullable(producer).orElseGet(() -> context.createProducerTemplate());
    }
}
