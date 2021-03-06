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

import android.util.Log;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import us.iseek.model.android.exceptions.HttpException;
import us.iseek.model.exception.UnknownLocationException;
import us.iseek.model.gps.Location;
import us.iseek.model.request.topic.CreateSubscriptionRequest;
import us.iseek.model.topics.HashTag;
import us.iseek.model.topics.Subscription;
import us.iseek.model.user.User;
import us.iseek.services.ITopicService;

/**
 * Invokes a REST web service to find topics being discussed in the system.
 *
 * @author Armando Valdes
 * @since 1.0
 */
public class TopicServiceRestAdapter implements ITopicService {

    private static final String TOPIC_PATH = "/topic";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HashTag> findTopics(Location location) {
        // Define connection parameters
        Log.d("FIND TOPICS", "location=" + location);
        String findUrl = RestConstants.BASE_URL + TOPIC_PATH
                + "/find?latitude=" + location.getLatitude()
                + "&longitude=" + location.getLongitude();

        try {
            // Call web service
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            ResponseEntity<HashTag[]> responseEntity =
                    restTemplate.getForEntity(findUrl, HashTag[].class);

            // Verify there is anything to parse
            if (responseEntity == null || responseEntity.getBody() == null) {
                return null;
            }

            // Parse response into list
            HashTag[] hashTagArray = responseEntity.getBody();
            List<HashTag> hashTags = new ArrayList<HashTag>();
            for (HashTag hashTag : hashTagArray) {
                hashTags.add(hashTag);
            }
            return hashTags;
        } catch (Exception e) {
            Log.e("HTTP_EXCEPTION", "Could not find topics", e);
            throw new HttpException("Could not find topics", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HashTag createTopic(String topicName) {
        // Define connection parameters
        Log.d("CREATE_TOPIC", "topicName=" + topicName);
        String createUrl = RestConstants.BASE_URL + TOPIC_PATH + "/create";

        try {
            // Call web service
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            HashTag topic = restTemplate.postForObject(createUrl, topicName, HashTag.class);
            return topic;
        } catch (Exception e) {
            Log.e("HTTP_EXCEPTION", "Could not create topic", e);
            throw new HttpException("Could not create topic", e);
        }
    }

    @Override
    public Subscription findSubscription(Long userId, Long topicId) {
        // Define connection parameters
        Log.d("FIND_SUBSCRIPTION",
                "userId=" + userId + ", topicId=" + topicId);
        String findSubscriptionsUrl = RestConstants.BASE_URL + TOPIC_PATH + "/subscription/find"
                + "?userId=" + userId
                + "&topicId=" + topicId;

        try {
            // Call web service
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            Subscription subscription =
                    restTemplate.getForObject(findSubscriptionsUrl, Subscription.class);
            return subscription;
        } catch (Exception e) {
            Log.e("HTTP_EXCEPTION", "Could not find subscription", e);
            throw new HttpException("Could not find subscription", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Subscription subscribe(Long userId, HashTag topic) throws UnknownLocationException {
        // Define connection parameters
        Log.d("SUBSCRIBE_TO_TOPIC", "userId=" + userId + ", topic=" + topic);
        String subscribeUrl = RestConstants.BASE_URL + TOPIC_PATH + "/subscription/create";

        // Create subscription request
        CreateSubscriptionRequest createSubscriptionRequest =
                new CreateSubscriptionRequest(userId, topic);

        try {
            // Call web service
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            Subscription subscription = restTemplate.postForObject(
                    subscribeUrl, createSubscriptionRequest, Subscription.class);
            return subscription;
        } catch (Exception e) {
            Log.e("HTTP_EXCEPTION", "Could not subscribe to topic", e);
            throw new HttpException("Could not subscribe to topic", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Subscription renewSubscription(Long subscriptionId) {
        // Define connection parameters
        Log.d("RENEW_SUBSCRIPTION", "subscriptionId=" + subscriptionId);
        String renewSubscriptionUrl = RestConstants.BASE_URL + TOPIC_PATH + "/subscription/renew";

        try {
            // Call web service
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            Subscription subscription = restTemplate.postForObject(
                    renewSubscriptionUrl, subscriptionId, Subscription.class);
            return subscription;
        } catch (Exception e) {
            Log.e("HTTP_EXCEPTION", "Could not renew subscription to topic", e);
            throw new HttpException("Could not renew subscription to topic", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> updateSubscription(Long aLong, Location location) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unSubscribe(Long aLong) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getUsersInTopic(HashTag hashTag, Location location) {
        return null;
    }
}
