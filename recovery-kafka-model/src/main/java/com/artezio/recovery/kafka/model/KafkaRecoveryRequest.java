package com.artezio.recovery.kafka.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;

import java.util.Date;

/**
 * Recovery request data structure for Kafka adapter.
 * <pre>
 * Fields:
 *  externalId (string) External message ID.
 *  locker (string) External code to lock new data storing if it exists.
 *  message (string) Short specific recovery data.
 *  pause (string) Recovery processing pause rule.
 *  processingFrom (date) Date to start redelivery processing.
 *  processingLimit (number) Limit of redelivery tries.
 *  processingTo (date) Date to interrupt redelivery processing.
 *  queue (string) Code to specify redelivery queue.
 *  queueParent (string) Code to specify parent redelivery queue.
 * </pre>
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Data
public class KafkaRecoveryRequest {

    private String externalId;
    private String locker;
    private String message;
    private String pause;
    private Date processingFrom;
    private Integer processingLimit;
    private Date processingTo;
    private String queue;
    private String queueParent;

    @SneakyThrows
    @Override
    public String toString() {
        return new ObjectMapper().writeValueAsString(this);
    }
}
