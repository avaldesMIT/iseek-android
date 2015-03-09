/**
 * Copyright 2010-present Facebook.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

import us.iseek.android.R;
import us.iseek.android.fragment.SelectionFragment;
import us.iseek.android.fragment.UserSettingsFragment;
import us.iseek.model.android.MenuItem;
import us.iseek.model.user.User;

public class MainActivity extends FragmentActivity implements ConnectionCallbacks,
        OnConnectionFailedListener, MenuDisplayingActivity {

    private static final String USER_SKIPPED_LOGIN_KEY = "user_skipped_login";

    private static final int SPLASH = 0;
    private static final int SELECTION = 1;
    private static final int SETTINGS = 2;
    private static final int USER_SETTINGS = 3;
    private static final int FRAGMENT_COUNT = USER_SETTINGS +1;

    // Current user
    private User currentUser;

    // Location aware variables
    private Double latitude;
    private Double longitude;
    private GoogleApiClient googleApiClient;

    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
    private boolean isResumed = false;
    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(),  PackageManager.GET_SIGNATURES);
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

        this.googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        this.googleApiClient.connect();

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        FragmentManager fm = getSupportFragmentManager();
        fragments[SPLASH] = fm.findFragmentById(R.id.splashFragment);;
        fragments[SELECTION] = fm.findFragmentById(R.id.selectionFragment);
        fragments[SETTINGS] = fm.findFragmentById(R.id.facebookSettingsFragment);
        fragments[USER_SETTINGS] = fm.findFragmentById(R.id.userSettingsFragment);

        FragmentTransaction transaction = fm.beginTransaction();
        for(int i = 0; i < fragments.length; i++) {
            transaction.hide(fragments[i]);
        }
        transaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
        isResumed = true;

        // Call the 'activateApp' method to log an app event for use in analytics and advertising reporting.  Do so in
        // the onResume methods of the primary Activities that an app may be launched into.
        AppEventsLogger.activateApp(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
        isResumed = false;

        // Call the 'deactivateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in the onPause methods of the primary Activities that an app may be launched into.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

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
     * Shows the topic selection fragment
     */
    public void showTopicSelectionFragment() {
        // Set user's settings
        ((SelectionFragment) this.fragments[SELECTION]).setUserValues(this.currentUser);

        // Show fragment
        showFragment(SELECTION, true);
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (isResumed) {
            FragmentManager manager = getSupportFragmentManager();
            int backStackSize = manager.getBackStackEntryCount();
            for (int i = 0; i < backStackSize; i++) {
                manager.popBackStack();
            }
            // check for the OPENED state instead of session.isOpened() since for the
            // OPENED_TOKEN_UPDATED state, the selection fragment should already be showing.
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
     * @return The the current user.
     */
    public User getCurrentUser() {
        return this.currentUser;
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
        }
    }
}
