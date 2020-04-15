package com.artezio.recovery.jms.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Recovery request data structure for JMS adapter.
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
@ToString
public class JMSRecoveryRequest implements Serializable {

    private String externalId;
    private String locker;
    private String message;
    private String pause;
    private Date processingFrom;
    private Integer processingLimit;
    private Date processingTo;
    private String queue;
    private String queueParent;
}
