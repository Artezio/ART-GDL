/*
 */
package com.artezio.recovery.server.data.access;

import com.artezio.recovery.server.data.messages.RecoveryOrder;
import java.util.Date;
import javax.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Data access operations for recovery orders.
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
public interface IRecoveryOrderCrud extends CrudRepository<RecoveryOrder, Long> {

    /**
     * Remove all successfully delivered recovery orders.
     * 
     * @return Number removed orders.
     */
    @Modifying
    @Query("DELETE FROM RecoveryOrder o WHERE o.status = com.artezio.recovery.server.data.types.RecoveryStatusEnum.SUCCESS")
    int cleanSuccessOrders();

    /**
     * Remove all recovery orders with delivery errors.
     * 
     * @return Number removed orders.
     */
    @Modifying
    @Query("DELETE FROM RecoveryOrder o WHERE o.status = com.artezio.recovery.server.data.types.RecoveryStatusEnum.ERROR")
    int cleanErrorOrders();

    /**
     * Resume recovery processing for orders which long time are not closed. 
    * 
     * @param resumingDate Resuming cutting date.
     * @return Number updated orders.
     */
    @Modifying
    @Query("UPDATE RecoveryOrder o SET o.versionId = NULL WHERE (o.versionId IS NOT NULL) AND (o.orderOpened < :resumingDate)")
    int resumeOrders(@Param("resumingDate") Date resumingDate);

    /**
     * Select with locking an recovery order for update.
     * 
     * @param id Recovery order ID.
     * @return Locked recovery order.
     */
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("SELECT o FROM RecoveryOrder o WHERE o.id = :orderId")
    RecoveryOrder findAndLock(@Param("orderId") Long id);
    
    /**
     * Find recovery orders which are not processed.
     * 
     * @param pageable Data paging settings.
     * @return Data page of recovery orders.
     */
    @Query("SELECT o FROM RecoveryOrder o WHERE"
            + " (o.versionId IS NULL)"
            + " AND (o.queue IS NULL)"
            + " AND (o.code = com.artezio.recovery.server.data.types.ProcessingCodeEnum.NEW)"
            + " AND (o.status = com.artezio.recovery.server.data.types.RecoveryStatusEnum.PROCESSING)"
            + " ORDER BY o.orderCreated ASC")
    Page<RecoveryOrder> findNewOrders(Pageable pageable);
    
    /**
     * Find processing recovery orders which are not queued.
     * 
     * @param pageable Data paging settings.
     * @return Data page of recovery orders.
     */
    @Query("SELECT o FROM RecoveryOrder o WHERE"
            + " (o.versionId IS NULL)"
            + " AND (o.queue IS NULL)"
            + " AND (o.status = com.artezio.recovery.server.data.types.RecoveryStatusEnum.PROCESSING)"
            + " ORDER BY o.orderOpened ASC")
    Page<RecoveryOrder> findProcessingOrders(Pageable pageable);
    
    /**
     * Find processing recovery orders which are queued.
     * 
     * @param pageable Data paging settings.
     * @return Data page of recovery orders.
     */
    @Query("SELECT o FROM RecoveryOrder o WHERE"
            + " (o.versionId IS NULL)"
            + " AND (o.queue IS NOT NULL)"
            + " AND (o.status = com.artezio.recovery.server.data.types.RecoveryStatusEnum.PROCESSING)"
            + " GROUP BY o.queue HAVING o.orderCreated = MIN(o.orderCreated)"
            + " ORDER BY o.queue ASC")
    Page<RecoveryOrder> findQueuedOrders(Pageable pageable);
    
}
