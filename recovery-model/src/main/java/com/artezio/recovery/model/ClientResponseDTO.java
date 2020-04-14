/*
 */
package com.artezio.recovery.model;

import com.artezio.recovery.server.data.messages.ClientResponse;
import lombok.Getter;
import lombok.experimental.Delegate;

import java.io.Serializable;

public class ClientResponseDTO implements Serializable {

    public ClientResponseDTO() {
        response = new ClientResponse();
    }

    @Getter
    @Delegate(types = ClientResponse.class)
    private ClientResponse response;

}
