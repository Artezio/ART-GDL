package com.artezio.recovery.storage.repository;

import com.artezio.recovery.storage.model.RecoveryMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Data access operations for recovery message.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Repository
public interface RecoveryMessageRepository extends CrudRepository<RecoveryMessage, Long> {
}
