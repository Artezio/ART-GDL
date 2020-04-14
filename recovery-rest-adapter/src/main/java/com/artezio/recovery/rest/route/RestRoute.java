package com.artezio.recovery.rest.route;

import com.artezio.recovery.model.RecoveryRequestDTO;
import com.artezio.recovery.processor.UnwrappingProcessor;
import com.artezio.recovery.rest.bean.RestCallbackBean;
import com.artezio.recovery.server.config.TransactionSupportConfig;
import com.artezio.recovery.server.data.messages.ClientResponse;
import com.artezio.recovery.server.context.RecoveryRoutes;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
     * REST callback route ID.
     */
    private static final String REST_CALLBACK_ROUTE_ID = "restCallbackRoute";

    /**
     * REST callback route URL.
     */
    public static final String REST_CALLBACK_ROUTE_URL = "direct://" + REST_CALLBACK_ROUTE_ID;

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

    /**
     * Processor for unwrapping from DTO.
     */
    @Autowired
    private UnwrappingProcessor unwrapping;

    /**
     * Recovery REST callback bean.
     */
    @Autowired
    private RestCallbackBean callbackBean;

    @Override
    public void configure() {

        restConfiguration()
                .host(serverHost).port(serverPort)
                .bindingMode(RestBindingMode.auto);

        rest()
                .post("/recover").id(POST_ENDPOINT_ID)
                .consumes("application/json")
                .type(RecoveryRequestDTO.class)
                .to(REST_ROUTE_URL);

        from(REST_ROUTE_URL).routeId(REST_ROUTE_ID)
                .transacted(TransactionSupportConfig.PROPAGATIONTYPE_PROPAGATION_REQUIRED)
                .process(unwrapping).id(UnwrappingProcessor.class.getSimpleName())
                .bean(callbackBean, "extractCallbackUri").id(RestCallbackBean.class.getSimpleName())
                .to("log:com.artezio.recovery.rest?level=DEBUG")
                .to(RecoveryRoutes.INCOME_URL);

        from(REST_CALLBACK_ROUTE_URL)
                .bean(callbackBean, "insertCallbackUri").id(RestCallbackBean.class.getSimpleName())
                .choice()
                .when(header("callbackUri").isEqualTo(null)).endChoice()
                .otherwise()
                .toD("${header.callbackUri}")
                .choice()
                .when(body().isInstanceOf(ClientResponse.class)).endChoice()
                .otherwise()
                .unmarshal().json(JsonLibrary.Jackson, ClientResponse.class);
    }
}
