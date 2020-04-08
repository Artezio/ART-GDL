/*
 */
package com.artezio.recovery.server.data.model;

import com.artezio.recovery.server.data.types.ClientResultEnum;
import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

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
