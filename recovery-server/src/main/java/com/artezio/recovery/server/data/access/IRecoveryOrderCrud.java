/*
 */
package com.artezio.recovery.server.data.access;

import com.artezio.recovery.server.data.messages.RecoveryOrder;
import org.springframework.data.repository.CrudRepository;

/**
 * CRUD operations for recovery request data.
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
public interface IRecoveryOrderCrud extends CrudRepository<RecoveryOrder, Long> {
    
}
