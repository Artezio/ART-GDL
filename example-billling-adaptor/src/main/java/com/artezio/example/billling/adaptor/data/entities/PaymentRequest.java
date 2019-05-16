/*
 */
package com.artezio.example.billling.adaptor.data.entities;

import com.artezio.example.billling.adaptor.data.types.PaymentOperationType;
import com.artezio.example.billling.adaptor.data.types.PaymentState;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

/**
 * Example external payment request.
 * <pre>
 *  id (number) Record identity.
 * 
 *  locker - External code to lock new data storing if it exists.
 *  pause - Recovery processing pause rule.
 *  processingFrom - Date to start redelivery processing.
 *  processingLimit - Limit of redelivery tries.
 *  processingTo - Date to interrupt redelivery processing.
 *  queue - Code to specify redelivery queue.
 *  queueParent - Code to specify parent redelivery queue.
 *
 *  amount - Payment amount.
 *  client - Billing client.
 *  successCount - Number of example tries to set payment success state. 
 *  operationType - Billing operation type.
 *  paymentState - Current payment state.
 * </pre>
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Entity
@Data
@SuppressWarnings("PersistenceUnitPresent")
@XmlRootElement
public class PaymentRequest implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 128)
    private String locker;
    @Column(length = 2000)
    private String pause;
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
    
    @Column
    private BigDecimal amount;
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private BillingClient client;
    @Column
    private Integer successCount;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentOperationType operationType;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentState paymentState;
}
