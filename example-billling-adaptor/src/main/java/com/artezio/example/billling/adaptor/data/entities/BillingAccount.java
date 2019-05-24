/*
 */
package com.artezio.example.billling.adaptor.data.entities;

import com.artezio.example.billling.adaptor.data.types.ClientAccountState;
import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;

/**
 * Billing client account.
 * <pre>
 * id - Account ID.
 * client - Billing client.
 * billingState - Billing account state.
 * balance - Account debit balance.
 * </pre>
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Entity
@Data
@SuppressWarnings("PersistenceUnitPresent")
@XmlRootElement
public class BillingAccount implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false, fetch = FetchType.EAGER)
    private BillingClient client;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ClientAccountState billingState;
    @Column(nullable = false)
    private BigDecimal balance;
            
}
