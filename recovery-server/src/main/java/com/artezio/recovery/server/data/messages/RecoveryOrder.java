/*
 */
package com.artezio.recovery.server.data.messages;

import com.artezio.recovery.server.data.types.HoldingCodeEnum;
import com.artezio.recovery.server.data.types.ProcessingCodeEnum;
import com.artezio.recovery.server.data.types.RecoveryStatusEnum;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

/**
 * Recovery request data structure for DB storing.
 * <pre>
 * Fields:
 *  id (number) Record identity.
 *  callbackUri (string) Callback route URI.
 *  code (enumeration) Short processing code.
 *  description (string) Processing description.
 *  externalId (string) External message ID.
 *  holdingCode (enumeration) Preprocessing hold code.
 *  locker (string) External code to lock new data storing if it exists.
 *  lockerVersion (string) Code to make with locker qnique index.
 *  message (string) Short specific recovery data.
 *  orderCreated (date) Record creation date.
 *  orderModified (date) Processing status modification date.
 *  orderOpened (date) Record opening access date.
 *  orderUpdated (date) Record closing access date.
 *  pause (string) Recovery processing pause rule.
 *  processingCount (number) Number of redelivery tries.
 *  processingFrom (date) Date to start redelivery processing.
 *  processingLimit (number) Limit of redelivery tries.
 *  processingTo (date) Date to interrupt redelivery processing.
 *  queue (string) Code to specify redelivery queue.
 *  queueParent (string) Code to specify parent redelivery queue.
 *  status (enumeration) Recovery request processing status.
 *  versionId (string) Data access session code.
 * </pre>
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Entity
@Data
@SuppressWarnings("PersistenceUnitPresent")
@XmlRootElement
@Table(indexes = {
    @Index(name = "idx_gd_status", unique = false, columnList = "status"),
    @Index(name = "idx_gd_version", unique = false, columnList = "versionId"),
    @Index(name = "idx_gd_locker", unique = true, columnList = "locker, lockerVersion"),
    @Index(name = "idx_gd_queue", unique = false, columnList = "queue"),
    @Index(name = "idx_gd_parent", unique = false, columnList = "queueParent")
})
public class RecoveryOrder implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2000, nullable = false)
    private String callbackUri;
    @Column(length = 128, nullable = false)
    private ProcessingCodeEnum code;
    @Column(length = 2000)
    private String description;
    @Column(length = 128)
    private String externalId;
    @Column(length = 128, nullable = false)
    private HoldingCodeEnum holdingCode;
    @Column(length = 128, nullable = false)
    private String locker;
    @Column(length = 128, nullable = false)
    private String lockerVersion;
    @Column(length = 2000)
    private String message;
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date orderCreated;
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date orderModified;
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date orderOpened;
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date orderUpdated;
    @Column(length = 2000)
    private String pause;
    @Column(nullable = false)
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
    private String queueParent;
    @Column(length = 128, nullable = false)
    private RecoveryStatusEnum status;
    @Column(length = 128)
    private String versionId;    

}
