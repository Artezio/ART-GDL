package com.artezio.recovery.kafka.model;

import com.artezio.recovery.server.data.types.HoldingCodeEnum;
import com.artezio.recovery.server.data.types.ProcessingCodeEnum;
import com.artezio.recovery.server.data.types.RecoveryStatusEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;

import java.util.Date;

/**
 * Recovery request data structure for Kafka adapter.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Data
public class KafkaRecoveryOrder {

    private Long id;
    private String callbackUri;
    private ProcessingCodeEnum code;
    private String description;
    private String externalId;
    private HoldingCodeEnum holdingCode;
    private String locker;
    private String lockerVersion;
    private String message;
    private Date orderCreated;
    private Date orderModified;
    private Date orderOpened;
    private Date orderUpdated;
    private String pause;
    private Integer processingCount;
    private Date processingFrom;
    private Integer processingLimit;
    private Date processingTo;
    private String queue;
    private String queueParent;
    private RecoveryStatusEnum status;
    private String versionId;

    @SneakyThrows
    @Override
    public String toString() {
        return new ObjectMapper().writeValueAsString(this);
    }
}
