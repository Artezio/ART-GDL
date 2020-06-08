package com.artezio.recovery.storage.processor;

import com.artezio.recovery.storage.model.RecoveryMessage;
import com.artezio.recovery.storage.repository.RecoveryMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Recovery message processor.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Slf4j
public class RecoveryMessageProcessor {

    /**
     * Recovery message length limit property.
     */
    @Value("${com.artezio.recovery.storage.limit:2000}")
    private int recoveryMessageLimit;

    /**
     * Repository for recovery messages.
     */
    @Autowired
    private RecoveryMessageRepository repository;

    /**
     * Process saving recovery request's message.
     *
     * @param message Recovery request's message.
     */
    public String processSaving(String message) {
        if (message != null && message.length() > recoveryMessageLimit) {
            return saveMessage(message);
        }
        return message;
    }

    /**
     * Process restoring recovery request's message.
     *
     * @param message Recovery request's message.
     */
    public String processRestoring(String message) {
        if (restoreMessage(message).isPresent()) {
            return restoreMessage(message).get().getMessage();
        }
        return message;
    }

    /**
     * Saving recovery request's message.
     *
     * @param message Recovery request's message.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.MANDATORY)
    String saveMessage(String message) {
        RecoveryMessage recoveryMessage = new RecoveryMessage();
        recoveryMessage.setMessage(message);
        String id = repository.save(recoveryMessage).getId().toString();
        log.info("Recovery message saved with id " + id);
        return id;
    }

    /**
     * Restoring recovery request's message.
     *
     * @param message Recovery request's message.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
    Optional<RecoveryMessage> restoreMessage(String message) {
        try {
            Long id = Long.valueOf(message);
            return repository.findById(id);
        } catch (NumberFormatException e) {
            log.info("There is no recovery message in DB");
            return Optional.empty();
        }
    }

}
