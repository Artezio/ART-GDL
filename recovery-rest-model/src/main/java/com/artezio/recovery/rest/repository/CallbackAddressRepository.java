package com.artezio.recovery.rest.repository;

import com.artezio.recovery.rest.model.CallbackAddress;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Data access operations for callback addresses.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Repository
public interface CallbackAddressRepository extends CrudRepository<CallbackAddress, Long> {
    /**
     * Select a callback address by external id.
     *
     * @param externalId external ID.
     * @return Locked recovery order.
     */
    @Query("SELECT o FROM CallbackAddress o WHERE o.externalId = :externalId")
    CallbackAddress findByExternalId(@Param("externalId") String externalId);
}
