/*
 */
package com.artezio.example.billling.adaptor.data.entities;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

/**
 * Billing client.
 * <pre>
 * id - Account ID.
 * firstName - Client first name.
 * lastName - Client last name.
 * account - Client billing account.
 * payments - Client payment request records.
 * </pre>
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Entity
@Data
@SuppressWarnings("PersistenceUnitPresent")
@XmlRootElement
public class BillingClient implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @OneToOne(optional = true, fetch = FetchType.EAGER,
            cascade = {
                CascadeType.PERSIST,
                CascadeType.REMOVE
            }
    )
    private BillingAccount account;
    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE})
    private Set<PaymentRequest> payments;
    
}
