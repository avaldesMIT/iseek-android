/**
 * Copyright (C) 2015 iSeek, Inc.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of iSeek, Inc.
 * You shall not disclose such confidential information and shall use it only
 * in accordance with the terms of the license agreement you entered into with
 * iSeek, Inc.
 */
package us.iseek.android.messaging;

import us.iseek.model.communication.MessageAbstract;

/**
 * A topic subscriber.
 *
 * @author Armando Valdes
 * @since 1.0
 */
public interface TopicSubscriber {

    /**
     * Receives a message published to the topic.
     *
     * @param message
     *              - The message that was published.
     */
    public void receiveMessage(MessageAbstract message);
}
