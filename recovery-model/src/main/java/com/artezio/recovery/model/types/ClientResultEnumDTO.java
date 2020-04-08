package com.artezio.recovery.model.types;

import lombok.Getter;

public enum ClientResultEnumDTO {

    SUCCESS(com.artezio.recovery.server.data.types.ClientResultEnum.SUCCESS),

    SYSTEM_ERROR(com.artezio.recovery.server.data.types.ClientResultEnum.SYSTEM_ERROR),

    BUSINESS_ERROR(com.artezio.recovery.server.data.types.ClientResultEnum.BUSINESS_ERROR),

    SYSTEM_FATAL_ERROR(com.artezio.recovery.server.data.types.ClientResultEnum.SYSTEM_FATAL_ERROR),

    BUSINESS_FATAL_ERROR(com.artezio.recovery.server.data.types.ClientResultEnum.BUSINESS_FATAL_ERROR);

    @Getter
    private com.artezio.recovery.server.data.types.ClientResultEnum result;

    ClientResultEnumDTO(com.artezio.recovery.server.data.types.ClientResultEnum result) {
        this.result = result;
    }
}
