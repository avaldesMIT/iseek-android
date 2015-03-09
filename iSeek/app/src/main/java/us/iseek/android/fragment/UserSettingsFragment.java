/**
 * Copyright (C) 2015 iSeek, Inc.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of iSeek, Inc.
 * You shall not disclose such confidential information and shall use it only
 * in accordance with the terms of the license agreement you entered into with
 * iSeek, Inc.
 */
package us.iseek.android.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

import us.iseek.android.activity.MainActivity;
import us.iseek.android.R;
import us.iseek.model.user.Preferences;
import us.iseek.model.user.User;
import us.iseek.services.IUserService;
import us.iseek.services.android.ServicesFactory;


public class UserSettingsFragment extends Fragment {
    private MainActivity activity;

    private Button saveButton;
    private Button cancelButton;
    private EditText userScreenName;
    private ToggleButton broadcastLocation;
    private ToggleButton showFacebookProfile;

    /**
     * Creates a new instance of this.
     */
    public UserSettingsFragment() {
        // Required empty public constructor
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = (MainActivity) getActivity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.user_settings, container, false);

        // Get references for UI elements
        this.saveButton = (Button) view.findViewById(R.id.saveButton);
        this.cancelButton = (Button) view.findViewById(R.id.cancelButton);
        this.userScreenName = (EditText) view.findViewById(R.id.userScreenName);
        this.broadcastLocation = (ToggleButton) view.findViewById(R.id.broadcastLocationToggle);
        this.showFacebookProfile = (ToggleButton) view.findViewById(R.id.showProfilePictureToggle);

        // Set button listeners
        this.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save settings
                UserSettingsFragment.this.saveSettings();
            }
        });

        this.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset settings
                UserSettingsFragment.this.setUserValues(
                        UserSettingsFragment.this.activity.getCurrentUser());
            }
        });

        // Return newly created view
        return view;
    }

    /**
     * Sets the user's values
     *
     * @param user
     *          - The user for whom the UI values will be set
     */
    public void setUserValues(User user) {
        // Set screen name
        this.userScreenName.setText(user.getScreenName());

        // Set other preferences
        Preferences preferences = user.getPreferences();
        this.broadcastLocation.setChecked(preferences.isBroadcastLocation());
        this.showFacebookProfile.setChecked(preferences.isShowProfilePicture());
    }

    /**
     * Saves the user's settings
     */
    private void saveSettings() {
        // Save screen name
        String newScreenName = this.userScreenName.getText().toString();
        if (!newScreenName.equals(this.activity.getCurrentUser().getScreenName())) {
            // Update local user
            this.activity.getCurrentUser().setScreenName(newScreenName);

            // Update database
            new UpdateUserScreenNameTask().execute(newScreenName);
        }

        // Save other preferences
        Boolean broadcastLocation = this.broadcastLocation.isChecked();
        Boolean showProfilePicture = this.showFacebookProfile.isChecked();
        Preferences newPreferences = new Preferences(broadcastLocation, showProfilePicture);
        if (!newPreferences.equals(this.activity.getCurrentUser().getPreferences())) {
            // Update local user
            this.activity.getCurrentUser().setPreferences(newPreferences);

            // Update database
            new UpdateUserPreferencesTask().execute(newPreferences);
        }
    }

    /**
     * Updates the user's screen name.
     */
    private class UpdateUserScreenNameTask extends AsyncTask<String, Void, User> {

        /**
         * {@inheritDoc}
         *
         * @param params
         *            - The screen name to update
         */
        @Override
        protected User doInBackground(String... params) {
            // Only update user if there is an active user
            if (UserSettingsFragment.this.activity.getCurrentUser() == null) {
                return null;
            }

            // Get user service
            IUserService userService = ServicesFactory.getInstance().createUserService();

            // Call web service
            String newScreenName = params[0];
            return userService.updateScreenName(
                    UserSettingsFragment.this.activity.getCurrentUser().getId(), newScreenName);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(User user) {
            // Track updated user
            if (user != null) {
                UserSettingsFragment.this.activity.setCurrentUser(user);
            }
        }
    }

    /**
     * Updates the user's preferences.
     */
    private class UpdateUserPreferencesTask extends AsyncTask<Preferences, Void, User> {

        /**
         * {@inheritDoc}
         *
         * @param params
         *            - The user's preferences to update
         */
        @Override
        protected User doInBackground(Preferences... params) {
            // Only update user if there is an active user
            if (UserSettingsFragment.this.activity.getCurrentUser() == null) {
                return null;
            }

            // Get user service
            IUserService userService = ServicesFactory.getInstance().createUserService();

            // Call web service
            Preferences newPreferences = params[0];
            return userService.updatePreferences(
                    UserSettingsFragment.this.activity.getCurrentUser().getId(), newPreferences);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(User user) {
            // Track updated user
            if (user != null) {
                UserSettingsFragment.this.activity.setCurrentUser(user);
            }
        }
    }
}
