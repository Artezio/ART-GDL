/*
 */
package com.artezio.recovery.model;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

import com.artezio.recovery.model.types.ClientResultEnum;

import lombok.Data;

/**
 * Recovery client response message.
 * <pre>
 *  result (enumeration) type of client processing result.
 *  description (string) Processing description.
 * </pre>
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Data
@XmlRootElement
public class ClientResponse implements Serializable {
    
    private ClientResultEnum result;
    private String description;
    
}
