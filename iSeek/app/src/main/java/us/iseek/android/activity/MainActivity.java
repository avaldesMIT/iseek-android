/**
 * Copyright (C) 2015 iSeek, Inc.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of iSeek, Inc.
 * You shall not disclose such confidential information and shall use it only
 * in accordance with the terms of the license agreement you entered into with
 * iSeek, Inc.
 */
package us.iseek.android.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;

import com.facebook.AppEventsLogger;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import us.iseek.android.R;
import us.iseek.android.fragment.TopicSelectionFragment;
import us.iseek.android.fragment.UserSettingsFragment;
import us.iseek.model.android.MenuItem;
import us.iseek.model.topics.HashTag;
import us.iseek.model.user.User;

/**
 * Defines the main activity of the application. This activity contains fragments that allow
 * the user to view topics being discussed in his/her immediate location, start new topics,
 * and participate in the discussion.
 */
public class MainActivity extends FragmentActivity implements ConnectionCallbacks,
        OnConnectionFailedListener, MenuDisplayingActivity {

    // Index of fragments contained in the activity
    private static final int SPLASH = 0;
    private static final int SELECTION = 1;
    private static final int SETTINGS = 2;
    private static final int USER_SETTINGS = 3;
    private static final int START_TOPIC = 4;
    private static final int FRAGMENT_COUNT = START_TOPIC + 1;
    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];

    // Current user
    private User currentUser;

    // Current topics
    private HashTag selectedTopic;
    private List<HashTag> userTopics;

    // Location aware variables
    private Double latitude;
    private Double longitude;
    private GoogleApiClient googleApiClient;

    // Facebook's UI lifecycle support
    private boolean isResumed = false;
    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // TODO: Remove code for final release version
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(),  PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures)
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Get location information
        this.googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        this.googleApiClient.connect();

        // Initialize Facebook session UI lifecycle management
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Initialize fragments
        FragmentManager fm = getSupportFragmentManager();
        fragments[SPLASH] = fm.findFragmentById(R.id.splashFragment);;
        fragments[SELECTION] = fm.findFragmentById(R.id.selectionFragment);
        fragments[SETTINGS] = fm.findFragmentById(R.id.facebookSettingsFragment);
        fragments[USER_SETTINGS] = fm.findFragmentById(R.id.userSettingsFragment);
        fragments[START_TOPIC] = fm.findFragmentById(R.id.startTopicFragment);

        // Hide fragments
        FragmentTransaction transaction = fm.beginTransaction();
        for(int i = 0; i < fragments.length; i++) {
            transaction.hide(fragments[i]);
        }
        transaction.commit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
        isResumed = true;

        // Log application activation for analytics/ads
        AppEventsLogger.activateApp(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
        isResumed = false;

        // Log application deactivation for analytics/ads
        AppEventsLogger.deactivateApp(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        Session session = Session.getActiveSession();

        if (session != null && session.isOpened()) {
            // If the session is already open, show the topic selection fragment
            showFragment(SELECTION, false);
        } else {
            // Otherwise show the splash screen with the Facebook Login option
            showFragment(SPLASH, false);
        }
    }

    /**
     * Shows the Facebook settings fragment on the main activity.
     */
    public void showFacebookSettingsFragment() {
        showFragment(SETTINGS, true);
    }

    /**
     * Shows the user settings fragment on the main activity.
     */
    public void showUserSettingsFragment() {
        // Set user's settings
        ((UserSettingsFragment) this.fragments[USER_SETTINGS]).setUserValues(this.currentUser);

        // Show fragment
        showFragment(USER_SETTINGS, true);
    }

    /**
     * Shows the topic topic_selection fragment
     */
    public void showTopicSelectionFragment() {
        // Set user's settings
        ((TopicSelectionFragment) this.fragments[SELECTION]).setUserValues(this.currentUser);

        // Show fragment
        showFragment(SELECTION, true);
    }

    /**
     * Shows the start topic fragment
     */
    public void showStartTopicFragment() {
        // Show fragment
        showFragment(START_TOPIC, true);
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (isResumed) {
            FragmentManager manager = getSupportFragmentManager();
            int backStackSize = manager.getBackStackEntryCount();
            for (int i = 0; i < backStackSize; i++) {
                manager.popBackStack();
            }
            // check for the OPENED state instead of session.isOpened() since for the
            // OPENED_TOKEN_UPDATED state, the topic_selection fragment should already be showing.
            if (state.equals(SessionState.OPENED)) {
                showFragment(SELECTION, false);
            } else if (state.isClosed()) {
                showFragment(SPLASH, false);
            }
        }
    }

    private void showFragment(int fragmentIndex, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            if (i == fragmentIndex) {
                transaction.show(fragments[i]);
            } else {
                transaction.hide(fragments[i]);
            }
        }
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastLocation != null) {
            this.latitude = lastLocation.getLatitude();
            this.longitude = lastLocation.getLongitude();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConnectionSuspended(int i) {
        // TODO: Handle case where connection to location services was suspended
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // TODO: Handle case where there is no Location connection
    }

    /**
     * Gets the last available latitude
     *
     * @return The last available latitude or null if no latitude was ever available.
     */
    public Double getLatitude() {
        return this.latitude;
    }

    /**
     * Gets the last available longitude
     *
     * @return The last available longitude or null if no longitude was ever available.
     */
    public Double getLongitude() {
        return this.longitude;
    }

    /**
     * Sets the current user
     *
     * @param currentUser
     *              - The currentUser to set.
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    /**
     * Gets the current user
     *
     * @return The currentUser.
     */
    public User getCurrentUser() {
        return this.currentUser;
    }

    /**
     * Sets the user's topics
     *
     * @param userTopics
     *              - The userTopics to set
     */
    public void setUserTopics(List<HashTag> userTopics) {
        this.userTopics = userTopics;
    }

    /**
     * Gets the user's topics.
     *
     * @return the userTopics.
     */
    public List<HashTag> getUserTopics() {
        return this.userTopics;
    }

    /**
     * Sets the selected topics
     *
     * @param selectedTopic
     *              - The selectedTopic to set
     */
    public void setSelectedTopic(HashTag selectedTopic) {
        this.selectedTopic = selectedTopic;
    }

    /**
     * Gets the selected topic.
     *
     * @return the selectedTopic.
     */
    public HashTag getSelectedTopic() {
        return this.selectedTopic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMenuSelectedCallback(MenuItem selectedItem) {
        // Determine fragment to display based on selected menu item
        switch (selectedItem) {
            case TOPICS_BUTTON:
                this.showTopicSelectionFragment();
                break;
            case SETTINGS_BUTTON:
                this.showUserSettingsFragment();
                break;
            case START_TOPIC_BUTTON:
                this.showStartTopicFragment();
                break;
        }
    }
}
