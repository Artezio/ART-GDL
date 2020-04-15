package com.artezio.recovery.jms.model;

import com.artezio.recovery.server.data.messages.RecoveryOrder;

import java.io.Serializable;

/**
 * Recovery request data structure for JMS adapter.
 *
 * @author Ilya Shevelev <Ilya.Shevelev@artezio.com>
 */
public class JMSRecoveryOrder extends RecoveryOrder implements Serializable {
}
