package com.artezio.recovery.rest.route;

import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.artezio.recovery.model.RecoveryRequest;
import com.artezio.recovery.server.config.TransactionSupportConfig;
import com.artezio.recovery.server.routes.RecoveryRoute;

import lombok.extern.slf4j.Slf4j;

/**
 * Recovery Apache Camel rest route class.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Component
@Slf4j
public class RestRoute extends SpringRouteBuilder {

    /**
     * POST endpoint ID.
     */
    public static final String POST_ENDPOINT_ID = "postEndpoint";

    /**
     * POST endpoint URL.
     */
    public static final String POST_ENDPOINT_URL = "rest:post:recover";

    /**
     * REST route ID.
     */
    public static final String REST_ROUTE_ID = "processPostRequest";

    /**
     * REST route URL.
     */
    private static final String REST_ROUTE_URL = "direct://" + REST_ROUTE_ID;

    /**
     * Server host property.
     */
    @Value("${rest.server.host:localhost}")
    private String serverHost;

    /**
     * Server port property.
     */
    @Value("${rest.server.port:8080}")
    private String serverPort;

    @Override
    public void configure() {

        restConfiguration()
            .component("restlet")
            .host(serverHost).port(serverPort)
            .bindingMode(RestBindingMode.auto);

        rest()
            .post("/recover").id(POST_ENDPOINT_ID)
            .consumes("application/json")
            .type(RecoveryRequest.class)
            .to(REST_ROUTE_URL);

        from(REST_ROUTE_URL).routeId(REST_ROUTE_ID)
            .transacted(TransactionSupportConfig.PROPAGATIONTYPE_PROPAGATION_REQUIRED)
            .to("log:com.artezio.recovery?level=DEBUG")
            .to(RecoveryRoute.INCOME_URL);
    }
}
