/*
 */
package com.artezio.example.billling.adaptor.web.pages;

import com.artezio.example.billling.adaptor.camel.BillingAdaptorRoute;
import com.artezio.example.billling.adaptor.data.entities.BillingClient;
import com.artezio.example.billling.adaptor.data.entities.PaymentRequest;
import com.artezio.example.billling.adaptor.services.BatchProcessing;
import com.artezio.example.billling.adaptor.services.ClientsManagement;
import com.artezio.example.billling.adaptor.services.ExampleDataGenerator;
import com.artezio.example.billling.adaptor.services.PaymentsManagement;
import com.artezio.example.billling.adaptor.services.types.PaymentStateCounter;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;

import com.artezio.recovery.server.data.types.DeliveryMethods;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Web user session bean.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@SessionScope
@ManagedBean
@Component
@Slf4j
public class IndexPageBean {

    /**
     * Test payments generation batch size.
     */
    public static final int GEN_SIZE = 10;
    /**
     * Payments data view page size.
     */
    public static final int PAGE_SIZE = 10;

    /**
     * Client management service.
     */
    @Autowired
    private ClientsManagement clintsManager;
    /**
     * Example data generation service.
     */
    @Autowired
    private ExampleDataGenerator dataGen;
    /**
     * Payments management service.
     */
    @Autowired
    private PaymentsManagement paymentsManager;
    /**
     * Payments management service.
     */
    @Autowired
    private BatchProcessing batchService;
    /**
     * Client list.
     */
    @Getter
    private List<BillingClient> clients;

    /**
     * Payments request list.
     */
    @Getter
    private List<PaymentRequest> payments;
    /**
     * Payment state counter.
     */
    @Getter
    private PaymentStateCounter stateCounter;
    /**
     * Processing ETA message.
     */

    @Getter
    private Map<String,DeliveryMethods> messageDeliveryMethodTypes;

    @Getter
    @Setter
    private DeliveryMethods selectedChannel;

    @Getter
    private String eta;
    /**
     * Example payment request.
     */
    private final PaymentRequest examplePayment = new PaymentRequest();
    /**
     * Start processing indicator.
     */
    private final AtomicBoolean started = new AtomicBoolean(false);
    /**
     * Start method is executing.
     */
    private final AtomicBoolean starting = new AtomicBoolean(false);
    /**
     * Stop method is executing.
     */
    private final AtomicBoolean canceling = new AtomicBoolean(false);
    /**
     * Async command thread keeper.
     */
    private final ExecutorService commandExecutor = Executors.newFixedThreadPool(1);
    /**
     * Payments data view current page number.
     */
    @Getter
    private long currentPage = 0;
    /**
     * Payments data view last page number.
     */
    @Getter
    private long lastPage = 0;

    /**
     * Bean post construct actions.
     */
    @PostConstruct
    public void createSession() {
        log.info("User session created.");
        dataGen.generateClientsIfEmpty();
        examplePayment.setSuccessCount(5);
        initDataStructures();
        loadViewData();
        if (stateCounter != null && stateCounter.getAll() <= 0) {
            generateData();
        }
    }

    /**
     * View data loading method.
     */
    public void loadViewData() {
        clients = new ArrayList<>(10);
        Set<String> ascSort = new HashSet<>();
        ascSort.add("firstName");
        List<BillingClient> data = clintsManager.getClientPage(0, 10, ascSort, null);
        if (data != null) {
            clients.addAll(data);
        }
        stateCounter = paymentsManager.countStates();
        long processing = batchService.countProcessingOrders();
        long paused = batchService.countPausedOrders();
        started.set(processing > 0);
        if (canceling.get()) {
            eta = "CANCELING";
        } else if (!started.get()) {
            eta = "NA";
        } else if (paused == processing) {
            eta = "PAUSED";
        } else if (stateCounter != null) {
            long mockTimeout = BillingAdaptorRoute.PRODUCER_TIMEOUT;
            long tries = paymentsManager.countSuccessTries();
            long min = (tries * mockTimeout) / 60_000;
            eta = String.valueOf(min) + " min";
        }
        lastPage = (long) stateCounter.getAll() / PAGE_SIZE;
        if (!(stateCounter.getAll() % PAGE_SIZE > 0)) {
            lastPage--;
        }
        if (currentPage < 0) {
            currentPage = lastPage;
        } else if (currentPage > lastPage) {
            currentPage = 0;
        }
        payments = new ArrayList<>(PAGE_SIZE);
        Set<String> descSort = new HashSet<>();
        descSort.add("id");
        List<PaymentRequest> paymentsData = paymentsManager.getPaymentsPage(
                (int) currentPage,
                PAGE_SIZE,
                null,
                descSort);
        if (paymentsData != null) {
            payments.addAll(paymentsData);
        }
    }

    /**
     * Page refresh timer listener.
     */
    public void timerListener() {
        loadViewData();
    }

    /**
     * Clean example data.
     */
    public void cleanData() {
        paymentsManager.removeAll();
        stateCounter = paymentsManager.countStates();
        loadViewData();
    }

    /**
     * Generate example data.
     */
    public void generateData() {
        if (stateCounter != null) {
            dataGen.generatePayments(GEN_SIZE, examplePayment);
        }
        loadViewData();
    }

    private void initDataStructures() {
        messageDeliveryMethodTypes = new LinkedHashMap<String, DeliveryMethods>();
        messageDeliveryMethodTypes.put("Direct Channel", DeliveryMethods.DIRECT); //label, value
        messageDeliveryMethodTypes.put("JMS Channel", DeliveryMethods.JMS);
        messageDeliveryMethodTypes.put("HTTP Channel", DeliveryMethods.HTTP);
    }

    /**
     * Disable start button flag.
     *
     * @return True if need to enable start button.
     */
    public boolean disableStartButton() {
        return (started.get() || canceling.get());
    }

    /**
     * Disable stop button flag.
     *
     * @return True if need to disable stop button.
     */
    public boolean disableStopButton() {
        return !started.get() || canceling.get();
    }

    /**
     * Start example billing processing.
     */
    public void start() {
        starting.set(true);
        commandExecutor.execute(() -> {
            try {
                batchService.startAll(selectedChannel);
            } finally {
                starting.set(false);
            }
        });
        loadViewData();
        started.set(true);
    }

    /**
     * Stop example billing processing.
     */
    public void stop() {
        canceling.set(true);
        if (started.get()) {
            commandExecutor.execute(() -> {
                try {
                    batchService.stopAll();
                } finally {
                    canceling.set(false);
                }
            });
        }
        loadViewData();
    }

    /**
     * Show next payments data page.
     */
    public void goNextPage() {
        currentPage++;
        loadViewData();
    }

    /**
     * Show previous payments data page.
     */
    public void goPreviousPage() {
        currentPage--;
        loadViewData();
    }

    /**
     * Show first payments data page.
     */
    public void goFirstPage() {
        currentPage = 0;
        loadViewData();
    }

    /**
     * Show last payments data page.
     */
    public void goLastPage() {
        currentPage = -1;
        loadViewData();
    }

    public void removeRequest(Long id) {
        if (id != null) {
            paymentsManager.remove(id);
        }
        loadViewData();
    }
}
