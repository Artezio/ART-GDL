/*
 */
package com.artezio.recovery.server.data.access;

import com.artezio.recovery.server.data.messages.RecoveryOrder;
import java.util.Date;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * CRUD operations for recovery request data.
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
public interface IRecoveryOrderCrud extends CrudRepository<RecoveryOrder, Long> {
    
    @Modifying
    @Query("DELETE FROM RecoveryOrder o WHERE o.status = com.artezio.recovery.server.data.types.RecoveryStatusEnum.SUCCESS")
    int cleanSuccessOrders();
    
    @Modifying
    @Query("DELETE FROM RecoveryOrder o WHERE o.status = com.artezio.recovery.server.data.types.RecoveryStatusEnum.ERROR")
    int cleanErrorOrders();
    
    @Modifying
    @Query("UPDATE RecoveryOrder o SET o.version = NULL WHERE (o.version IS NOT NULL) AND o.access < :resumingDate")
    int resumeOrders(@Param("resumingDate") Date resumingDate);
    
}
