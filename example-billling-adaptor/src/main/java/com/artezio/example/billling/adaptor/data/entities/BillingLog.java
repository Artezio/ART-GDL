/*
 */
package com.artezio.example.billling.adaptor.data.entities;

import com.artezio.example.billling.adaptor.data.types.BillingOperationType;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

/**
 * Billing operations log.
 * <pre>
 * id - Log ID.
 * client - Billing client.
 * operationType - Billing operation type.
 * description - Processing description.
 * </pre>
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Entity
@Data
@SuppressWarnings("PersistenceUnitPresent")
@XmlRootElement
public class BillingLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    @ManyToOne
    private BillingClient client;
    @Column(nullable = false)
    private BillingOperationType operationType;
    @Column(nullable = false, length = 4000)
    private String description;
    
}
