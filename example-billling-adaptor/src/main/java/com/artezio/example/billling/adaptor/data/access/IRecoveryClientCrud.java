/*
 */
package com.artezio.example.billling.adaptor.data.access;

import com.artezio.recovery.model.RecoveryOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Data access operations for recovery objects.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Repository
public interface IRecoveryClientCrud extends JpaRepository<RecoveryOrder, Long> {

    /**
     * Count all processing recovery orders.
     * 
     * @return Number of all processing recovery orders.
     */
    @Query("SELECT COUNT(o) FROM RecoveryOrder o WHERE"
            + " o.status = com.artezio.recovery.model.types.RecoveryStatusEnum.PROCESSING")
    long countProcessingOrders();
    
    /**
     * Count paused processing recovery orders.
     * 
     * @return Number of paused processing recovery orders.
     */
    @Query("SELECT COUNT(o) FROM RecoveryOrder o WHERE"
            + " o.status = com.artezio.recovery.model.types.RecoveryStatusEnum.PROCESSING"
            + " AND o.holdingCode != com.artezio.recovery.model.types.HoldingCodeEnum.NO_HOLDING")
    long countPausedOrders();
    
}
