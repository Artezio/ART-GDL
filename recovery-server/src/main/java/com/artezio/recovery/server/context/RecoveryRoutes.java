/*
 */
package com.artezio.recovery.server.context;

import com.artezio.recovery.server.data.access.IRecoveryOrderCrud;
import com.artezio.recovery.server.processors.CallbackProcessor;
import com.artezio.recovery.server.processors.CleaningProcessor;
import com.artezio.recovery.server.processors.RestoringProcessor;
import com.artezio.recovery.server.processors.ResumingProcessor;
import com.artezio.recovery.server.processors.StoringProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ExchangePattern;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Recovery Apache Camel routes class.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Component
@Slf4j
public class RecoveryRoutes extends SpringRouteBuilder {

    /**
     * Timer route ID.
     */
    public static final String TIMER_ID = "ScheduleTimerRoute";
    /**
     * Timer route URL.
     */
    private static final String TIMER_URL = "timer://" + TIMER_ID;
    /**
     * SEDA route ID.
     */
    public static final String SEDA_ID = "ScheduleSedaRoute";
    /**
     * SEDA route URL.
     */
    private static final String SEDA_URL = "seda://" + SEDA_ID;
    /**
     * Cleaning route ID.
     */
    public static final String CLEANING_ID = "ScheduleCleaningRoute";
    /**
     * Cleaning route URL.
     */
    private static final String CLEANING_URL = "timer://" + CLEANING_ID;
    /**
     * Income route ID.
     */
    public static final String INCOME_ID = "IncomeRoute";
    /**
     * Income route URL.
     */
    public static final String INCOME_URL = "direct://" + INCOME_ID;
    /**
     * Income route URI.
     */
    private static final String INCOME_URI = INCOME_URL;
    
    /**
     * Schedule timer period property.
     */
    @Value("${com.artezio.recovery.timer.period:100}")
    private String timerPeriod;
    /**
     * Amount of SEDA concurrent consumers property.
     */
    @Value("${com.artezio.recovery.seda.consumers:10}")
    private int sedaConsumers;
    /**
     * Schedule cleaning period property.
     */
    @Value("${com.artezio.recovery.timer.period:15m}")
    private String cleaningPeriod;
    /**
     *  Property of flag to schedule processing.
     */
    @Value("${com.artezio.recovery.schedule.enebled:true}")
    private boolean scheduleEnabled;
    
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
     * Resuming error records processor.
     */
    @Autowired
    private ResumingProcessor resuming;
    /**
     * Cleaning old data records processor.
     */
    @Autowired
    private CleaningProcessor cleaning;
    /**
     * Recovery request storing processor.
     */
    @Autowired
    private StoringProcessor storing;
    /**
     * Data access object.
     */
    @Autowired
    private IRecoveryOrderCrud dao;

    /**
     * Recovery Apache Camel routes definition.
     *
     * @throws Exception @see Exception
     */
    @Override
    public void configure() throws Exception {
        // Initializing data access.
        dao.count();
        // Define income route.
        log.debug(INCOME_URI);
        from(INCOME_URI)
                .routeId(INCOME_ID)
                .setExchangePattern(ExchangePattern.InOut)
                .process(storing);
        if (!scheduleEnabled) {
            return;
        }
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
        // Define resuming and cleaning activities.
        final String CLEANING_URI = CLEANING_URL + "?period=" + cleaningPeriod;
        log.debug(CLEANING_URI);
        from(CLEANING_URI)
                .routeId(CLEANING_ID)
                .process(resuming)
                .process(cleaning);
    }

}
