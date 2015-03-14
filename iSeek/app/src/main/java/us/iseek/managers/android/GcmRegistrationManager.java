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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import us.iseek.model.user.User;
import us.iseek.services.IUserService;
import us.iseek.services.android.ServicesFactory;

/**
 * Manages the GCM registrations. Abstracts the GCM registration process for the rest of the
 * application.
 *
 * @author Armando Valdes
 * @since 1.0
 */
public class GcmRegistrationManager {
    private static GcmRegistrationManager SINGLETON_INSTANCE;

    private static final String GCM_SENDER_ID = "989610008281";
    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private User user;
    private String pendingRegistrationId;
    private Map<Context, GoogleCloudMessaging> gcmContextMap;

    /**
     * Creates a new instance of this
     */
    private GcmRegistrationManager() {
        this.gcmContextMap = new HashMap<Context, GoogleCloudMessaging>();
    }

    /**
     * Gets a singleton instance of this.
     *
     * @return the singleton instance of this.
     */
    public static GcmRegistrationManager getInstance() {
        if (GcmRegistrationManager.SINGLETON_INSTANCE == null) {
            GcmRegistrationManager.SINGLETON_INSTANCE = new GcmRegistrationManager();
        }
        return GcmRegistrationManager.SINGLETON_INSTANCE;
    }

    /**
     * Registers the application to GCM.
     *
     * @param context
     *              - The context this application is being registered in.
     */
    public void registerInBackground(Context context) {
        // Determine if the application is already registered
        String registrationId = this.getRegistrationId(context);
        if (registrationId == null) {
            // If not already registered, register application in background
            new RegisterApplicationTask().execute(context);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     *
     * @param activity
     *              - The activity to check
     */
    public boolean checkPlayServices(Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.e("DEVICE_NOT_SUPPORTED", "This device is not supported.");
                activity.finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service if available. Note that
     * if the application is updated, any previous registration IDs are invalidated, so this method
     * will return null in that case.
     *
     * @return The registrationID or null if there is no existing registrationID.
     */
    private String getRegistrationId(Context context) {
        // Get registration ID from preferences
        final SharedPreferences preferences = this.getGcmPreferences(context);
        String registrationId = preferences.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            return null;
        }

        // Verify the application has not been updated since the last registration
        int currentVersion = this.getAppVersion(context);
        int registeredVersion = preferences.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        if (registeredVersion != currentVersion) {
            Log.d("APPLICATION_UPDATE_DETECTED",
                    "Application version changed from "
                            + registeredVersion + " to " + currentVersion);
            return null;
        }
        Log.d("RETRIEVED_REGISTRATION", "registrationId=" + registrationId);
        return registrationId;
    }

    /**
     * A task to register the application with the GCM servers. Also stores the
     * registration ID and the application's version code in the application's
     * shared preferences.
     *
     * If the registration fails for any reason, this task will retry registration after
     * a certain number of seconds. The wait time will be incremented exponentially until
     * all retry attempts are exhausted.
     */
    private class RegisterApplicationTask extends AsyncTask<Context, Void, String> {
        private static final int MAXIMUM_NUMBER_OF_ATTEMPTS = 15;

        private Context context;

        /**
         * {@inheritDoc}
         */
        @Override
        protected String doInBackground(Context... params) {
            // Get context from parameters
            this.context = params[0];

            // Get an instance of GCM if not already available
            GoogleCloudMessaging gcm = GcmRegistrationManager.this.gcmContextMap.get(this.context);
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(this.context);
                GcmRegistrationManager.this.gcmContextMap.put(this.context, gcm);
            }

            // Retry registration a certain number of tries
            for (int i = 0; i < MAXIMUM_NUMBER_OF_ATTEMPTS; i++) {
                try {
                    // Verify if this is a retry
                    if (i > 0) {
                        // Wait exponentially longer on each try
                        long backOffTime = 1000 * (2 ^ i);
                        Thread.sleep(backOffTime);
                    }

                    // Register application on GCM
                    return gcm.register(GCM_SENDER_ID);
                } catch (InterruptedException e) {
                    Log.e("SLEEP_ERROR", "There was an error waiting for back off time", e);
                } catch (IOException e) {
                    Log.e("GCM_REGISTRATION_ERROR", "There was an error registering app to GCM", e);
                }
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(String registrationId) {
            // Determine if registration was successful
            if (registrationId == null) {
                Log.e("COULD_NOT_COMPLETE_REGISTRATION", "Aborted registration.");
                return;
            }

            // Log registration complete
            Log.d("REGISTRATION_COMPLETE", "registrationId=" + registrationId);

            // Send the registration ID to the server so that it can push messages to this app
            if (GcmRegistrationManager.this.user == null) {
                // Wait to send the information until the user is available
                GcmRegistrationManager.this.pendingRegistrationId = registrationId;
            } else {
                new SendRegistrationToServerTask().execute(registrationId);
            }

            // Store the registration ID in the application preferences
            GcmRegistrationManager.this.storeRegistrationId(this.context, registrationId);
        }
    }

    /**
     * Stores the registration ID and the app versionCode in the application's shared preferences.
     *
     * @param context
     *              - The application's context to use in getting the application's version.
     * @param registrationId
     *              - The registration ID to store.
     */
    private void storeRegistrationId(Context context, String registrationId) {
        // Get application's version from context
        int applicationVersion = this.getAppVersion(context);
        Log.d("SAVING_REGISTRATION_ID", "appVersion=" + applicationVersion);

        // Store registration ID and application version in preferences
        final SharedPreferences preferences = this.getGcmPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PROPERTY_REG_ID, registrationId);
        editor.putInt(PROPERTY_APP_VERSION, applicationVersion);
        editor.commit();
    }

    /**
     * Retrieves the application's version code from the PackageManager.
     *
     * @return the application's versionCode
     */
    private int getAppVersion(Context context) {
        try {
            // Get version code from package manager
            PackageInfo packageInfo =
                    context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name from context", e);
        }
    }

    /**
     * Returns the application's shared preferences
     *
     * @return Application's GCM preferences
     */
    private SharedPreferences getGcmPreferences(Context context) {
        return context.getSharedPreferences(GcmRegistrationManager.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * Invokes a web service to store the user's GCM registration in the iSeek service.
     * Waits until the user is registered to the iSeek service before sending the registration
     * request.
     */
    private class SendRegistrationToServerTask extends AsyncTask<String, Void, User> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected User doInBackground(String... params) {
            // Get context from parameters
            String gcmRegistrationId = params[0];

            // Call web service
            IUserService userService = ServicesFactory.getInstance().createUserService();
            return userService.updateGcmRegistrationId(
                    GcmRegistrationManager.this.user.getId(), gcmRegistrationId);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(User user) {
            // There's nothing to do
        }
    }

    /**
     * Sets the user to register.
     *
     * @param user
     *          - The user to register.
     */
    public void setUser(User user) {
        this.user = user;

        // If the GCM registration ID was not sent to the server, do so now
        if (this.pendingRegistrationId != null) {
            // Send registration ID to iSeek server
            new SendRegistrationToServerTask().execute(this.pendingRegistrationId);

            // Clear pending action
            this.pendingRegistrationId = null;
        }
    }
}
