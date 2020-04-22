package com.artezio.recovery.kafka.model;

import com.artezio.recovery.server.data.types.ClientResultEnum;
import lombok.Data;

/**
 * Recovery client response message for Kafka adapter.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Data
public class KafkaClientResponse {
    private ClientResultEnum result;
    private String description;
}
