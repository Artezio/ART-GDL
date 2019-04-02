/*
 */
package com.artezio.recovery.server.routes.schedule;

import com.artezio.recovery.server.processors.CallbackProcessor;
import com.artezio.recovery.server.processors.RestoringProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ExchangePattern;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Recovery schedule Apache Camel routes class.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Component
@Slf4j
public class ScheduleRoute extends SpringRouteBuilder {

    /**
     * Timer route ID.
     */
    public static final String TIMER_ID = "ScheduleTimerRoute";
    /**
     * Timer route URL.
     */
    private static final String TIMER_URL = "timer://" + TIMER_ID;
    /**
     * Schedule timer period property.
     */
    @Value("${com.artezio.recovery.timer.period:100}")
    private String timerPeriod;
    /**
     * SEDA route ID.
     */
    public static final String SEDA_ID = "ScheduleSedaRoute";
    /**
     * SEDA route URL.
     */
    private static final String SEDA_URL = "seda://" + SEDA_ID;
    /**
     * Amount of SEDA concurrent consumers property.
     */
    @Value("${com.artezio.recovery.seda.consumers:10}")
    private int sedaConsumers;
    /**
     * Recovery callback processor.
     */
    @Autowired
    private CallbackProcessor callback;
    /**
     * Recovery request restoring processor.
     */
    @Autowired
    private RestoringProcessor restoring;

    /**
     * Recovery schedule Apache Camel routes definition.
     *
     * @throws Exception @see Exception
     */
    @Override
    public void configure() throws Exception {
        // Define schedule timer.
        final String TIMER_URI = TIMER_URL + "?period=" + timerPeriod;
        log.debug(TIMER_URI);
        from(TIMER_URI)
                .routeId(TIMER_ID)
                .doTry().to(SEDA_URL)
                .doCatch(Throwable.class).to("mock:dump");
        // Define schedule consumers.
        final String SEDA_URI = SEDA_URL
                + "?waitForTaskToComplete=Never"
                + "&concurrentConsumers=" + String.valueOf(sedaConsumers)
                + "&discardIfNoConsumers=true";
        log.debug(SEDA_URI);
        from(SEDA_URI)
                .routeId(SEDA_ID)
                .setExchangePattern(ExchangePattern.InOnly)
                .process(restoring)
                .process(callback);
    }

}
