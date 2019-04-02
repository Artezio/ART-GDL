/*
 */
package com.artezio.recovery.server.data.messages;

import com.artezio.recovery.server.data.types.ProcessingCodeEnum;
import com.artezio.recovery.server.data.types.RecoveryStatusEnum;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

/**
 * Recovery request data structure for DB storing.
 * <pre>
 * Fields:
 *  id (number) Record identity.
 *  access (date) Record access date.
 *  callbackUri (string) Callback URI.
 *  code (enumeration) Short processing code.
 *  created (date) Record creation date.
 *  description (string) Processing description.
 *  externalId (string) External message ID.
 *  locker (string) External code to lock new data storing if it exists.
 *  message (string) Short specific recovery data.
 *  modified (date) Processing status modification date.
 *  parentQueue (string) Code to specify parent redelivery queue.
 *  pause (string) Recovery processing pause rule.
 *  processingCount (number) Number of redelivery tries.
 *  processingFrom (date) Date to start redelivery processing.
 *  processingLimit (number) Limit of redelivery tries.
 *  processingTo (date) Date to interrupt redelivery processing.
 *  queue (string) Code to specify redelivery queue.
 *  session (string) Data access session code.
 *  status (enumeration) Recovery request processing status.
 *  version (string) Data access session code.
 * </pre>
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Entity
@Data
@SuppressWarnings("PersistenceUnitPresent")
@XmlRootElement
public class RecoveryOrder implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date access;
    @Column(length = 2000)
    private String callbackUri;
    @Column(length = 128)
    private ProcessingCodeEnum code;
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Column(length = 2000)
    private String description;
    @Column(length = 128)
    private String externalId;
    @Column(length = 128)
    private String locker;
    @Column(length = 2000)
    private String message;
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;
    @Column(length = 128)
    private String parentQueue;
    @Column(length = 2000)
    private String pause;
    @Column
    private Integer processingCount;
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date processingFrom;
    @Column
    private Integer processingLimit;
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date processingTo;
    @Column(length = 128)
    private String queue;
    @Column(length = 128)
    private RecoveryStatusEnum status;
    @Column(length = 128)
    private String version;

}
