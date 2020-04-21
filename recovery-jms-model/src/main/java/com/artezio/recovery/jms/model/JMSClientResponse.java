package com.artezio.recovery.jms.model;

import com.artezio.recovery.server.data.types.ClientResultEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * Recovery client response message for JMS adapter.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Data
public class JMSClientResponse implements Serializable {
    private ClientResultEnum result;
    private String description;
}
