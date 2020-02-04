package com.artezio.recovery.server.adapters;

import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.artezio.recovery.server.config.TransactionSupportConfig;
import com.artezio.recovery.server.context.RecoveryRoutes;
import com.artezio.recovery.server.data.access.IRecoveryOrderCrud;
import com.artezio.recovery.server.data.messages.RecoveryRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * Recovery Apache Camel routes class.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Component
@Slf4j
public class RestAdapter extends SpringRouteBuilder implements BaseAdapter {

    /**
     * POST endpoint ID.
     */
    private static final String POST_ENDPOINT_ID = "postEndpoint";

    /**
     * POST endpoint URL.
     */
    public static final String POST_ENDPOINT_URL = "rest:post:recover";

    /**
     * REST route ID.
     */
    private static final String REST_ROUTE_ID = "processPostRequest";

    /**
     * REST route URL.
     */
    private static final String REST_ROUTE_URL = "direct://" + REST_ROUTE_ID;

    /**
     * Data access object.
     */
    @Autowired
    private IRecoveryOrderCrud dao;

    /**
     * Server host property.
     */
    @Value("${com.artezio.recovery.server.host:localhost}")
    private String serverHost;

    /**
     * Server port property.
     */
    @Value("${com.artezio.recovery.server.port:8080}")
    private String serverPort;

    @Override
    public void configure() {

        restConfiguration()
            .component("restlet")
            .host(serverHost).port(serverPort);

        rest()
            .post("/recover").id(POST_ENDPOINT_ID).to(REST_ROUTE_URL);

        from(REST_ROUTE_URL).routeId(REST_ROUTE_ID)
            .transacted(TransactionSupportConfig.PROPAGATIONTYPE_PROPAGATION_REQUIRED)
            .unmarshal().json(JsonLibrary.Jackson, RecoveryRequest.class)
            .to("log:com.artezio.recovery?level=DEBUG")
            .to(RecoveryRoutes.INCOME_URL);
    }
}
