/*
 */
package com.artezio.recovery.server.data.access;

import com.artezio.recovery.server.data.messages.RecoveryOrder;
import java.util.Date;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * Set processing version.
     *
     * @param orderId Recovery order ID.
     * @param versionId Processing version ID.
     * @return Number updated orders.
     */
    @Modifying
    @Query("UPDATE RecoveryOrder o SET o.versionId = :versionId WHERE o.id = :orderId")
    int updateVersion(
            @Param("orderId") Long orderId,
            @Param("versionId") String versionId);

    /**
     * Select an order by specific processing version.
     *
     * @param versionId Processing version ID.
     * @return Locked recovery order.
     */
    @Query("SELECT o FROM RecoveryOrder o WHERE o.versionId = :versionId")
    RecoveryOrder findOrderByVersionId(@Param("versionId") String versionId);

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
    @Query("SELECT o FROM RecoveryOrder o WHERE "
            + "o.status = com.artezio.recovery.server.data.types.RecoveryStatusEnum.PROCESSING "
            + "GROUP BY o.queue, o.versionId "
            + "HAVING (o.orderCreated = MIN(o.orderCreated)) AND (COUNT(o.versionId) = 0) "
            + "ORDER BY o.queue ASC")
    Page<RecoveryOrder> findQueuedOrders(Pageable pageable);

    /**
     * Find top of processing messages in a queue.
     *
     * @param pageable Data paging settings.
     * @param queue Code of a queue.
     * @param created Message creation date.
     * @return Data page of recovery orders.
     */
    @Query("SELECT o FROM RecoveryOrder o WHERE"
            + " (o.versionId IS NULL)"
            + " AND (o.queue = :queue)"
            + " AND (o.orderCreated < :created)"
            + " AND (o.status = com.artezio.recovery.server.data.types.RecoveryStatusEnum.PROCESSING)"
            + " ORDER BY o.orderCreated ASC")
    Page<RecoveryOrder> findTopOfQueue(Pageable pageable,
            @Param("queue") String queue,
            @Param("created") Date created);

    /**
     * Find processing messages in a parent queue.
     *
     * @param pageable Data paging settings.
     * @param queueParent Code of a parent queue.
     * @return Data page of recovery orders.
     */
    @Query("SELECT o FROM RecoveryOrder o WHERE"
            + " (o.versionId IS NULL)"
            + " AND (o.queueParent = :queueParent)"
            + " AND (o.status = com.artezio.recovery.server.data.types.RecoveryStatusEnum.PROCESSING)"
            + " ORDER BY o.orderCreated ASC")
    Page<RecoveryOrder> findParentQueue(Pageable pageable,
            @Param("queueParent") String queueParent);

}
