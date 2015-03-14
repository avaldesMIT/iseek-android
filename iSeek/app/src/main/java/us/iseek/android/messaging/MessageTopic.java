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

import java.util.ArrayList;
import java.util.List;

import us.iseek.model.communication.MessageAbstract;

/**
 * A topic to publish messages to subscribers.
 *
 * @author Armando Valdes
 * @since 1.0
 */
public class MessageTopic {

    private static MessageTopic SINGLETON_INSTANCE;

    private List<TopicSubscriber> topicSubscribers;

    /**
     * Creates a new instance of this
     */
    private MessageTopic() {
        this.topicSubscribers = new ArrayList<TopicSubscriber>();
    }

    /**
     * Gets a singleton instance of this.
     *
     * @return the singleton instance of this.
     */
    public static MessageTopic getInstance() {
        if (MessageTopic.SINGLETON_INSTANCE == null) {
            MessageTopic.SINGLETON_INSTANCE = new MessageTopic();
        }
        return MessageTopic.SINGLETON_INSTANCE;
    }

    /**
     * Subscribes the subscriber to the message topic.
     *
     * @param topicSubscriber
     *                  - The subscriber to subscribe.
     */
    public void subscribe(TopicSubscriber topicSubscriber) {
        synchronized (this.topicSubscribers) {
            this.topicSubscribers.add(topicSubscriber);
        }
    }

    /**
     * Unsubscribes the subscriber from the message topic.
     *
     * @param topicSubscriber
     *                  - The subscriber to unsubscribe.
     */
    public void unSubscribe(TopicSubscriber topicSubscriber) {
        synchronized (this.topicSubscribers) {
            this.topicSubscribers.remove(topicSubscriber);
        }
    }

    /**
     * Publishes the message to the subscribers.
     *
     * @param message
     *              - The message to publish.
     */
    public void publish(MessageAbstract message) {
        synchronized (this.topicSubscribers) {
            for (TopicSubscriber topicsubscriber : this.topicSubscribers) {
                topicsubscriber.receiveMessage(message);
            }
        }
    }
}
