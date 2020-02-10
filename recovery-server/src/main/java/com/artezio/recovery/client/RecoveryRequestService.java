package com.artezio.recovery.client;

import java.util.HashMap;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.artezio.recovery.server.data.exception.RecoveryException;
import com.artezio.recovery.server.data.model.RecoveryRequest;
import com.artezio.recovery.server.data.types.DeliveryMethodType;
import com.artezio.recovery.server.routes.RecoveryRoute;
import com.artezio.recovery.server.routes.adapters.JMSAdapter;
import com.artezio.recovery.server.routes.adapters.RestAdapter;

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
     * Sends recovery request depending on delivery method.
     *
     * @param request Recovery request that should be send.
//     * @param context Camel context.
     * @param deliveryMethodType Delivery method type for choosing producer.
     */
//    public void sendRequest(DeliveryMethodType deliveryMethodType, CamelContext context, RecoveryRequest request)
    public void sendRequest(DeliveryMethodType deliveryMethodType, RecoveryRequest request)
        throws RecoveryException {
//        ProducerTemplate producer = getProducer(deliveryMethodType, context);
        ProducerTemplate producer = getProducer(deliveryMethodType);
        producer.sendBody(request);
    }

    public boolean isServerStarted() {
        List<Route> routes = context.getRoutes();
        for (Route route : routes) {
            System.out.println(route.getId());
        }
        return context.getStatus().isStarted();
    }

    public void stopRoutes() throws Exception {
        List<Route> routes = context.getRoutes();
        for (Route route : routes) {
            context.stopRoute(route.getId());
        }
        context.stop();
    }

    /**
     * Returns producer depending on delivery type.
     *
     * @param deliveryMethodType delivery type.
//     * @param context Camel context.
     * @return producer.
     */
//    private ProducerTemplate getProducer(DeliveryMethodType deliveryMethodType, CamelContext context)
    private ProducerTemplate getProducer(DeliveryMethodType deliveryMethodType)
        throws RecoveryException {
        ProducerTemplate producer = context.createProducerTemplate();
        switch (deliveryMethodType) {
            case JMS:
                producer.setDefaultEndpointUri(JMSAdapter.JMS_QUEUE_ROUTE_URL);
                break;
            case REST:
                producer.setDefaultEndpointUri(RestAdapter.POST_ENDPOINT_URL + "?host=" + host + ":" + port);
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
}
