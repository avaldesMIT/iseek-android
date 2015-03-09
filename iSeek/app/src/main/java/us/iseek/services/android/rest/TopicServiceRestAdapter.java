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

import us.iseek.model.android.HttpException;
import us.iseek.model.exception.UnknownLocationException;
import us.iseek.model.gps.Location;
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
    public HashTag createTopic(String s) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Subscription subscribe(Long aLong, HashTag hashTag) throws UnknownLocationException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Subscription renewSubscription(Long aLong) {
        return null;
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
