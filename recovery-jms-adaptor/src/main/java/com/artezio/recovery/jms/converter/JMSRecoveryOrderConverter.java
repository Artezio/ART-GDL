package com.artezio.recovery.jms.converter;


import com.artezio.recovery.jms.model.JMSRecoveryOrder;
import com.artezio.recovery.server.data.messages.RecoveryOrder;
import com.artezio.recovery.storage.processor.RecoveryMessageProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Converter;
import org.apache.camel.TypeConverters;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;

/**
 * Recovery order converter for JMS adapter.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Slf4j
public class JMSRecoveryOrderConverter implements TypeConverters {

    /**
     * Processor for storing recovery messages.
     */
    @Autowired
    private RecoveryMessageProcessor messageProcessor;

    @Converter
    public JMSRecoveryOrder convert(RecoveryOrder order) throws InvocationTargetException, IllegalAccessException {
        JMSRecoveryOrder jmsOrder = new JMSRecoveryOrder();
        BeanUtils.copyProperties(jmsOrder, order);
        jmsOrder.setMessage(messageProcessor.processRestoring(order.getMessage()));
        return jmsOrder;
    }
}
