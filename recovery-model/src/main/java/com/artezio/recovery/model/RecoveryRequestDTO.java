package com.artezio.recovery.model;

import com.artezio.recovery.server.data.model.RecoveryRequest;
import lombok.Getter;
import lombok.experimental.Delegate;

import java.io.Serializable;

public class RecoveryRequestDTO implements Serializable {

    public RecoveryRequestDTO() {
        recoveryRequest = new RecoveryRequest();
    }

    public RecoveryRequestDTO(RecoveryRequest recoveryRequest) {
        this.recoveryRequest = recoveryRequest;
    }

    @Getter
    @Delegate(types = RecoveryRequest.class)
    private RecoveryRequest recoveryRequest;
}
