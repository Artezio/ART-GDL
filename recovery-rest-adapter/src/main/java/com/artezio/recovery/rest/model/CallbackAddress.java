package com.artezio.recovery.rest.model;

import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

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
