/*
 */
package com.artezio.recovery.server.data.messages;

import java.io.Serializable;
import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

/**
 * Recovery request data structure for DB storing.
 * <pre>
 * Fields:
 *  callbackUri (string) Callback URI.
 *  externalId (string) External message ID.
 *  locker (string) External code to lock new data storing if it exists.
 *  message (string) Short specific recovery data.
 *  parentQueue (string) Code to specify parent redelivery queue.
 *  pause (string) Recovery processing pause rule.
 *  processingFrom (date) Date to start redelivery processing.
 *  processingLimit (number) Limit of redelivery tries.
 *  processingTo (date) Date to interrupt redelivery processing.
 *  queue (string) Code to specify redelivery queue.
 * </pre>
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Data
@XmlRootElement
public class RecoveryRequest implements Serializable {


    private String callbackUri;
    private String externalId;
    private String locker;
    private String message;
    private String parentQueue;
    private String pause;
    private Date processingFrom;
    private Integer processingLimit;
    private Date processingTo;
    private String queue;

}
