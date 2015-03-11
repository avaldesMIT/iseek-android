/**
 * Copyright (C) 2015 iSeek, Inc.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of iSeek, Inc.
 * You shall not disclose such confidential information and shall use it only
 * in accordance with the terms of the license agreement you entered into with
 * iSeek, Inc.
 */
package us.iseek.services.android;

import java.util.List;

import us.iseek.model.exception.UnknownLocationException;
import us.iseek.model.gps.Location;
import us.iseek.model.topics.HashTag;
import us.iseek.model.topics.Subscription;
import us.iseek.model.user.User;
import us.iseek.services.ITopicService;

/**
 * Provides services to find topics being discussed in the system.
 *
 * @author Armando Valdes
 * @since 1.0
 */
public class TopicService implements ITopicService {

    private ITopicService restAdapter;

    /**
     * Creates a new instance of this
     */
    protected TopicService() {
        // Default constructor
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HashTag> findTopics(Location location) {
        return this.restAdapter.findTopics(location);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HashTag createTopic(String displayName) {
        return this.restAdapter.createTopic(displayName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Subscription findSubscription(Long userId, Long topicId) {
        return this.restAdapter.findSubscription(userId, topicId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Subscription subscribe(Long userId, HashTag hashTag) throws UnknownLocationException {
        return this.restAdapter.subscribe(userId, hashTag);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Subscription renewSubscription(Long subscriptionId) {
        return this.restAdapter.renewSubscription(subscriptionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> updateSubscription(Long subscriptionId, Location location) {
        return this.restAdapter.updateSubscription(subscriptionId, location);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unSubscribe(Long subscriptionId) {
        this.restAdapter.unSubscribe(subscriptionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getUsersInTopic(HashTag hashTag, Location location) {
        return this.restAdapter.getUsersInTopic(hashTag, location);
    }

    /**
     * @return the restAdapter
     */
    public ITopicService getRestAdapter() {
        return restAdapter;
    }

    /**
     * @param restAdapter
     *            the restAdapter to set
     */
    public void setRestAdapter(ITopicService restAdapter) {
        this.restAdapter = restAdapter;
    }
}
