/**
 * Copyright (C) 2015 iSeek, Inc.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of iSeek, Inc.
 * You shall not disclose such confidential information and shall use it only
 * in accordance with the terms of the license agreement you entered into with
 * iSeek, Inc.
 */
package us.iseek.services.android.rest;

import us.iseek.model.communication.Message;
import us.iseek.model.exception.UnsupportedMessageTypeException;
import us.iseek.services.IMessageService;

/**
 * Invokes a REST web service to send messages to other users or groups of users, or to
 * broadcast messages to a topic.
 *
 * @author Armando Valdes
 * @since 1.0
 */
public class MessageServiceRestAdapter implements IMessageService {

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(Message message) throws UnsupportedMessageTypeException {

    }
}
