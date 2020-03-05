package com.artezio.recovery.rest.bean;

import com.artezio.recovery.server.data.model.RecoveryRequest;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import static com.artezio.recovery.rest.route.RestRoute.REST_CALLBACK_ROUTE_URL;

/**
 * Callback bean for rest adapter.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Component
public class RestCallbackBean {

    /**
     * Extract callbackUri from request to global options.
     *
     * @param exchange Apache Camel ESB exchange message.
     */
    public void extractCallbackUri(Exchange exchange) {
        RecoveryRequest recoveryRequest = exchange.getIn().getBody(RecoveryRequest.class);
        String callbackUri = recoveryRequest.getCallbackUri();
        if (callbackUri != null && !callbackUri.isEmpty()) {
            exchange.getContext().getGlobalOptions().put("restCallback", callbackUri);
        }
        recoveryRequest.setCallbackUri(REST_CALLBACK_ROUTE_URL);
        exchange.getIn().setBody(recoveryRequest);
    }

    /**
     * Insert callbackUri to header.
     *
     * @param exchange Apache Camel ESB exchange message.
     */
    public void insertCallbackUri(Exchange exchange) {
        String callbackUri = exchange.getContext().getGlobalOptions().get("restCallback");
        if (callbackUri != null && !callbackUri.isEmpty()) {
            exchange.getIn().setHeader("callbackUri", callbackUri);
        }
    }
}
