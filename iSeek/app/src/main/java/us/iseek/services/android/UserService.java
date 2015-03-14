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

import us.iseek.model.gps.Location;
import us.iseek.model.user.Preferences;
import us.iseek.model.user.User;
import us.iseek.services.IUserService;

/**
 * Provides services to manage users preferences and lifecycle.
 *
 * @author Armando Valdes
 * @since 1.0
 */
public class UserService implements IUserService {

    private IUserService restAdapter;

    /**
     * Creates a new instance of this
     */
    protected UserService() {
        // Default constructor
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User get(Long userId) {
        return this.restAdapter.get(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getByFacebookProfileId(Long facebookProfileId) {
        return this.restAdapter.getByFacebookProfileId(facebookProfileId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User createUser(Long userId, Location location) {
        return this.restAdapter.createUser(userId, location);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User updateGcmRegistrationId(Long userId, String gcmRegistrationId) {
        return this.restAdapter.updateGcmRegistrationId(userId, gcmRegistrationId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User updatePreferences(Long userId, Preferences preferences) {
        return this.restAdapter.updatePreferences(userId, preferences);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User updateLocation(Long userId, Location location) {
        return this.restAdapter.updateLocation(userId, location);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User updateScreenName(Long userId, String screenName) {
        return this.restAdapter.updateScreenName(userId, screenName);
    }

    /**
     * @return the restAdapter
     */
    public IUserService getRestAdapter() {
        return restAdapter;
    }

    /**
     * @param restAdapter
     *            the restAdapter to set
     */
    public void setRestAdapter(IUserService restAdapter) {
        this.restAdapter = restAdapter;
    }
}
