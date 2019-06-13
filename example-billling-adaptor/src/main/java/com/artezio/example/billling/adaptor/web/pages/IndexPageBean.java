/*
 */
package com.artezio.example.billling.adaptor.web.pages;

import com.artezio.example.billling.adaptor.data.entities.BillingClient;
import com.artezio.example.billling.adaptor.data.entities.PaymentRequest;
import com.artezio.example.billling.adaptor.services.BatchProcessing;
import com.artezio.example.billling.adaptor.services.ClientsManagement;
import com.artezio.example.billling.adaptor.services.ExampleDataGenerator;
import com.artezio.example.billling.adaptor.services.PaymentsManagement;
import com.artezio.example.billling.adaptor.services.types.PaymentStateCounter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import lombok.Getter;
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
    public static final int GEN_SIZE = 4;
    /**
     * Payments data view page size.
     */
    public static final int PAGE_SIZE = 20;

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
        loadViewData();
        if (stateCounter != null && stateCounter.getAll() <= 0) {
            generateData();
        }
    }

    /**
     * View data loading method.
     */
    public void loadViewData() {
        clients = new ArrayList<>(1000);
        Set<String> ascSort = new HashSet<>();
        ascSort.add("firstName");
        List<BillingClient> data = clintsManager.getClientPage(0, 1000, ascSort, null);
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
            eta = String.valueOf((long) (stateCounter.getRegistered() + stateCounter.getProcessing()) / 12) + " min";
        }
        lastPage = (long) stateCounter.getAll() / PAGE_SIZE;
        if (stateCounter.getAll() % PAGE_SIZE > 0) {
            lastPage++;
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
            for (BillingClient c : clients) {
                if (c.getId() != null) {
                    examplePayment.setClient(c);
                    dataGen.generatePayments(GEN_SIZE, examplePayment);
                }
            }
            stateCounter = paymentsManager.countStates();
        }
        loadViewData();
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
                batchService.startAll();
            } finally {
                starting.set(false);
            }
        });
        loadViewData();
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
    public void nextPage() {
        currentPage++;
        loadViewData();
    }
    
    /**
     * Show previous payments data page.
     */
    public void prevPage() {
        currentPage--;
        loadViewData();
    }
    
    /**
     * Show first payments data page.
     */
    public void firstPage() {
        currentPage = 0;
        loadViewData();
    }
    
    /**
     * Show last payments data page.
     */
    public void lastPage() {
        currentPage = -1;
        loadViewData();
    }

}