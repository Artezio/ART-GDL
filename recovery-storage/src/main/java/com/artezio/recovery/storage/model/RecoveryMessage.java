package com.artezio.recovery.storage.model;

import lombok.Data;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Recovery message data structure for DB storing.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Entity
@Data
@SuppressWarnings("PersistenceUnitPresent")
@XmlRootElement
public class RecoveryMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = Integer.MAX_VALUE, nullable = false)
    @Lob
    private String message;
}

