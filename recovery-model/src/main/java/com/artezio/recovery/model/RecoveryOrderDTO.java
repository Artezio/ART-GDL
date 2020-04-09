package com.artezio.recovery.model;

import com.artezio.recovery.server.data.model.RecoveryOrder;
import lombok.Getter;
import lombok.experimental.Delegate;

public class RecoveryOrderDTO {

    public RecoveryOrderDTO() {
        recoveryOrder = new RecoveryOrder();
    }

    public RecoveryOrderDTO(RecoveryOrder recoveryOrder) {
        this.recoveryOrder = recoveryOrder;
    }

    @Getter
    @Delegate(types = RecoveryOrder.class)
    private RecoveryOrder recoveryOrder;

}
