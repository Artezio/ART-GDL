/*
 */
package com.artezio.example.billling.adaptor.web;

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
public class UserSession {

    /**
     * Test payments generation batch size.
     */
    public static final int GEN_SIZE = 4;

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
     * Bean post construct actions.
     */
    @PostConstruct
    public void createSession() {
        log.info("User session created.");
        dataGen.generateClientsIfEmpty();
        loadClients();
        examplePayment.setSuccessCount(5);
        stateCounter = paymentsManager.countStates();
        if (stateCounter != null && stateCounter.getAll() <= 0) {
            generateData();
        }
    }

    public void loadClients() {
        clients = new ArrayList<>();
        Set<String> ascSort = new HashSet<>();
        ascSort.add("firstName");
        List<BillingClient> data = clintsManager.getClientPage(0, 1000, ascSort, null);
        if (data != null) {
            clients.addAll(data);
        }
    }

    /**
     * Page refresh timer listener.
     */
    public void timerListener() {
        stateCounter = paymentsManager.countStates();
        loadClients();
        if (canceling.get()) {
            eta = "CANCELING";
        } else if (stateCounter != null) {
            eta = String.valueOf((long) (stateCounter.getRegistered() + stateCounter.getProcessing()) / 12) + " min";
        }
        if (!starting.get() && started.get() && stateCounter != null) {
            if ((stateCounter.getProcessing() <= 0 && stateCounter.getRegistered() <= 0)
                    || stateCounter.getAll() <= 0) {
                started.set(false);
            }
        } else if (stateCounter == null) {
            started.set(false);
        }
    }

    /**
     * Clean example data.
     */
    public void cleanData() {
        paymentsManager.removeAll();
        stateCounter = paymentsManager.countStates();
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
        started.set(true);
        starting.set(true);
        commandExecutor.execute(() -> {
            try {
                batchService.startAll();
            } finally {
                starting.set(false);
            }
        });
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
                    started.set(false);
                    canceling.set(false);
                }
            });
        }
    }

}
