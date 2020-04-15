package com.artezio.recovery.rest.model;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class RestRecoveryRequest {

    private String callbackUri;
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
