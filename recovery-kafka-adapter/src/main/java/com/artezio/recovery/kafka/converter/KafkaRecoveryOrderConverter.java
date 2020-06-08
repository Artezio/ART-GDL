package com.artezio.recovery.kafka.converter;

import com.artezio.recovery.kafka.model.KafkaRecoveryOrder;
import com.artezio.recovery.server.data.messages.RecoveryOrder;
import com.artezio.recovery.storage.processor.RecoveryMessageProcessor;
import org.apache.camel.Converter;
import org.apache.camel.TypeConverters;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;

/**
 * Recovery order converter for Kafka adapter.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class KafkaRecoveryOrderConverter implements TypeConverters {

    /**
     * Processor for storing recovery messages.
     */
    @Autowired
    private RecoveryMessageProcessor messageProcessor;

    @Converter
    public KafkaRecoveryOrder convert(RecoveryOrder order) throws InvocationTargetException, IllegalAccessException {
        KafkaRecoveryOrder kafkaOrder = new KafkaRecoveryOrder();
        BeanUtils.copyProperties(kafkaOrder, order);
        kafkaOrder.setMessage(messageProcessor.processRestoring(order.getMessage()));
        return kafkaOrder;
    }
}