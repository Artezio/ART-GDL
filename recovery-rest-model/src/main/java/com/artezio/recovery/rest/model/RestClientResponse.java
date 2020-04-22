package com.artezio.recovery.rest.model;

import com.artezio.recovery.server.data.types.ClientResultEnum;
import lombok.Data;

/**
 * Recovery client response message for Rest adapter.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Data
public class RestClientResponse {

    private ClientResultEnum result;
    private String description;
}
