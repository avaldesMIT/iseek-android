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

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.util.Log;

import us.iseek.model.android.exceptions.HttpException;
import us.iseek.model.gps.Location;
import us.iseek.model.request.user.CreateUserRequest;
import us.iseek.model.request.user.UpdateUserLocationRequest;
import us.iseek.model.request.user.UpdateUserPreferencesRequest;
import us.iseek.model.request.user.UpdateUserScreenNameRequest;
import us.iseek.model.user.Preferences;
import us.iseek.model.user.User;
import us.iseek.services.IUserService;

/**
 * Invokes a REST web service to manage users preferences and lifecycle.
 *
 * @author Armando Valdes
 * @since 1.0
 */
public class UserServiceRestAdapter implements IUserService {

    private static final String USER_PATH = "/user";

    /**
     * {@inheritDoc}
     */
    @Override
    public User get(Long userId) {
        // Define connection parameters
        Log.d("GET_USER", "userId=" + userId);
        String getUrl = RestConstants.BASE_URL + USER_PATH + "/get?userId=" + userId;

        try {
            // Call web service
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            User user = restTemplate.getForObject(getUrl, User.class);
            return user;
        } catch (Exception e) {
            Log.e("HTTP_EXCEPTION", "Could not get user", e);
            throw new HttpException("Could not get user", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getByFacebookProfileId(Long facebookProfileId) {
        // Define connection parameters
        Log.d("GET_USER BY FACEBOOK PROFILE ID", "facebookProfileId=" + facebookProfileId);
        String getByFacebookProfileIdUrl = RestConstants.BASE_URL + USER_PATH
                + "/getByFacebookProfileId?userId=" + facebookProfileId;

        try {
            // Call web service
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            User user = restTemplate.getForObject(getByFacebookProfileIdUrl, User.class);
            return user;
        } catch (Exception e) {
            Log.e("HTTP_EXCEPTION", "Could not get user by facebook profile id", e);
            throw new HttpException("Could not get user by facebook profile id", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User createUser(Long userId, Location location) {
        // Create request
        CreateUserRequest createUserRequest = new CreateUserRequest(userId, location);

        // Define connection parameters
        Log.d("CREATE_USER", "userId=" + userId + ",location=" + location);
        String createUrl = RestConstants.BASE_URL + USER_PATH + "/create";

        try {
            // Call web service
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            User user = restTemplate.postForObject(createUrl, createUserRequest, User.class);
            return user;
        } catch (Exception e) {
            Log.e("HTTP_EXCEPTION", "Could not create user", e);
            throw new HttpException("Could not create user", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User updatePreferences(Long userId, Preferences preferences) {
        // Create request
        UpdateUserPreferencesRequest updateUserPreferencesRequest = 
                new UpdateUserPreferencesRequest(userId, preferences);

        // Define connection parameters
        Log.d("UPDATE USER PREFERENCES", "userId=" + userId + ",preferences=" + preferences);
        String updatePreferencesUrl = RestConstants.BASE_URL + USER_PATH + "/updatePreferences";

        try {
            // Call web service
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            User user = restTemplate.postForObject(
                    updatePreferencesUrl, updateUserPreferencesRequest, User.class);
            return user;
        } catch (Exception e) {
            Log.e("HTTP_EXCEPTION", "Could not update user's preferences", e);
            throw new HttpException("Could not update user's preferences", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User updateLocation(Long userId, Location location) {
        // Create request
        UpdateUserLocationRequest updateUserLocationRequest =
                new UpdateUserLocationRequest(userId, location);

        // Define connection parameters
        Log.d("UPDATE USER LOCATION", "userId=" + userId + ",location=" + location);
        String updateLocationUrl = RestConstants.BASE_URL + USER_PATH + "/updateLocation";

        try {
            // Call web service
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            User user = restTemplate.postForObject(
                    updateLocationUrl, updateUserLocationRequest, User.class);
            return user;
        } catch (Exception e) {
            Log.e("HTTP_EXCEPTION", "Could not update user's location", e);
            throw new HttpException("Could not update user's location", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User updateScreenName(Long userId, String screenName) {
        // Create request
        UpdateUserScreenNameRequest updateUserScreenNameRequest =
                new UpdateUserScreenNameRequest(userId, screenName);

        // Define connection parameters
        Log.d("UPDATE USER SCREEN NAME", "userId=" + userId + ",screenName=" + screenName);
        String updateScreenNameUrl = RestConstants.BASE_URL + USER_PATH + "/updateScreenName";

        try {
            // Call web service
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            User user = restTemplate.postForObject(
                    updateScreenNameUrl, updateUserScreenNameRequest, User.class);
            return user;
        } catch (Exception e) {
            Log.e("HTTP_EXCEPTION", "Could not update user's screen name", e);
            throw new HttpException("Could not update user's screen name", e);
        }
    }
}
