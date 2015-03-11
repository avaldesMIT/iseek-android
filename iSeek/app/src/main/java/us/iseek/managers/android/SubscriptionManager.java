/**
 * Copyright (C) 2015 iSeek, Inc.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of iSeek, Inc.
 * You shall not disclose such confidential information and shall use it only
 * in accordance with the terms of the license agreement you entered into with
 * iSeek, Inc.
 */
package us.iseek.managers.android;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import us.iseek.model.android.callbacks.RequestCallback;
import us.iseek.model.exception.UnknownLocationException;
import us.iseek.model.gps.Location;
import us.iseek.model.topics.HashTag;
import us.iseek.model.topics.Subscription;
import us.iseek.model.user.User;
import us.iseek.services.ITopicService;
import us.iseek.services.android.ServicesFactory;

/**
 * Manages the user's subscriptions. Abstracts the logic of subscribing to a topic.
 *
 * @author Armando Valdes
 * @since 1.0
 */
public class SubscriptionManager {

    private static SubscriptionManager SINGLETON_INSTANCE;

    /**
     * Creates a new instance of this
     */
    private SubscriptionManager() {
        // Hide singleton constructor
    }

    /**
     * Gets a singleton instance of this.
     *
     * @return the singleton instance of this.
     */
    public static SubscriptionManager getInstance() {
        if (SubscriptionManager.SINGLETON_INSTANCE == null) {
            SubscriptionManager.SINGLETON_INSTANCE = new SubscriptionManager();
        }
        return SubscriptionManager.SINGLETON_INSTANCE;
    }

    /**
     * Subscribes the user to the given topic. If the user is already subscribed to the topic,
     * this method renews the user's subscription.
     *
     * @param user
     *              - The user who will be subscribed to the topic
     * @param topic
     *              - The topic to subscribe the user to
     * @param callback
     *              - The callback to invoke if the operation is successful
     */
    public void subscribeUser(User user, HashTag topic, RequestCallback<Subscription> callback) {
        new SubscribeUserToTopicTask(user, callback).execute(topic);
    }

    /**
     * Subscribes the user to the topic provided.
     */
    private class SubscribeUserToTopicTask extends AsyncTask<HashTag, Void, Subscription> {

        private User user;
        private RequestCallback<Subscription> callback;

        /**
         * Creates a new instance of this.
         *
         * @param user
         *              - The user to subscribe
         * @param callback
         *              - The callback to invoke if the operation is successful
         */
        public SubscribeUserToTopicTask(User user, RequestCallback<Subscription> callback) {
            this.user = user;
            this.callback = callback;
        }

        /**
         * {@inheritDoc}
         *
         * @param params
         *            - The topic to subscribe the user to
         */
        @Override
        protected Subscription doInBackground(HashTag... params) {
            // Get method parameters
            HashTag topic = params[0];
            Long userId = this.user.getId();
            Location location = this.user.getLastLocation();

            // Get topic service
            ITopicService topicService = ServicesFactory.getInstance().createTopicService();

            // Verify user is not already subscribed to topic
            Subscription currentSubscription =
                    topicService.findSubscription(userId, topic.getId());
            if (currentSubscription != null) {
                // Renew subscription
                Log.d("RENEWING_SUBSCRIPTION", "subscription=" + currentSubscription);
                return topicService.renewSubscription(currentSubscription.getId());
            } else {
                // Subscribe user
                try {
                    return topicService.subscribe(userId, topic);
                } catch (UnknownLocationException e) {
                    Log.e("SUBSCRIPTION_FAILED", "Could not subscribe to topic", e);
                    return null;
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(Subscription subscription) {
            if (subscription != null) {
                Log.d("SUBSCRIPTION_SUCCESSFUL", "subscription=" + subscription);
                this.callback.success(subscription);
            }
        }
    }
}
