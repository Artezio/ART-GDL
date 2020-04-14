package com.artezio.recovery.server.processors;

import com.artezio.recovery.server.data.access.IRecoveryOrderCrud;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cleaning old data records processor.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Slf4j
public class CleaningProcessor implements Processor {
    
    /**
     * Data access object.
     */
    @Autowired
    private IRecoveryOrderCrud dao;
    
    /**
     *  Property of flag to allow successfully processed orders cleaning.
     */
    @Value("${com.artezio.recovery.cleaning.success:true}")
    private boolean cleaningSuccess;
    /**
     *  Property of flag to allow failed orders cleaning.
     */
    @Value("${com.artezio.recovery.cleaning.error:true}")
    private boolean cleaningError;
    
    /**
     * Cleaning old data records process definition.
     *
     * @param exchange Apache Camel ESB exchange message.
     * @throws Exception @see Exception
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
    public void process(Exchange exchange) throws Exception {
        if (cleaningSuccess) {
            dao.cleanSuccessOrders();
        }
        if (cleaningError) {
            dao.cleanErrorOrders();
        }
    }

}
