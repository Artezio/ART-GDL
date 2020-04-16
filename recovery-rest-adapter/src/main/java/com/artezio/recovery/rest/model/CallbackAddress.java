package com.artezio.recovery.rest.model;

import lombok.Data;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Callback address data structure for DB storing.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Entity
@Data
@SuppressWarnings("PersistenceUnitPresent")
@XmlRootElement
public class CallbackAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2000, nullable = false)
    private String externalId;

    @Column(length = 2000, nullable = false)
    private String callbackUri;
}
