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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.ProfilePictureView;

import java.util.ArrayList;
import java.util.List;

import us.iseek.android.R;
import us.iseek.android.activity.MainActivity;
import us.iseek.android.view.element.TopicListElement;
import us.iseek.android.view.element.TopicListElementArrayAdapter;
import us.iseek.model.gps.Location;
import us.iseek.model.topics.HashTag;
import us.iseek.model.user.User;
import us.iseek.services.ITopicService;
import us.iseek.services.IUserService;
import us.iseek.services.android.ServicesFactory;

/**
 * A fragment for topic topic_selection.
 */
public class TopicSelectionFragment extends Fragment {

    private static final String PENDING_ANNOUNCE_KEY = "pendingAnnounce";
    private static final Uri M_FACEBOOK_URL = Uri.parse("http://m.facebook.com");

    private static final int REAUTH_ACTIVITY_CODE = 100;
    private static final String PERMISSION = "publish_actions";

    private ListView listView;
    private List<TopicListElement> topicElements;

    private TextView userScreenName;
    private ProfilePictureView profilePictureView;

    private MainActivity activity;
    private UiLifecycleHelper uiHelper;

    private boolean pendingAnnounce;

    private Session.StatusCallback sessionCallback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, final SessionState state, final Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    private FacebookDialog.Callback nativeDialogCallback = new FacebookDialog.Callback() {
        @Override
        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
            boolean resetSelections = true;
            if (FacebookDialog.getNativeDialogDidComplete(data)) {
                if (FacebookDialog.COMPLETION_GESTURE_CANCEL
                        .equals(FacebookDialog.getNativeDialogCompletionGesture(data))) {
                    // Leave selections alone if user canceled.
                    resetSelections = false;
                    showCancelResponse();
                } else {
                    showSuccessResponse(FacebookDialog.getNativeDialogPostId(data));
                }
            }

            if (resetSelections) {
                init(null);
            }
        }

        @Override
        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
            new AlertDialog.Builder(getActivity())
                    .setPositiveButton(R.string.error_dialog_button_text, null)
                    .setTitle(R.string.error_dialog_title)
                    .setMessage(error.getLocalizedMessage())
                    .show();
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        uiHelper = new UiLifecycleHelper(getActivity(), sessionCallback);
        uiHelper.onCreate(savedInstanceState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.topic_selection, container, false);

        this.listView = (ListView) view.findViewById(R.id.topic_list);
        this.userScreenName = (TextView) view.findViewById(R.id.userScreenName);
        this.profilePictureView = (ProfilePictureView) view.findViewById(R.id.selection_profile_pic);

        // Configure Facebook profile picture view
        this.profilePictureView.setCropped(true);
        this.profilePictureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TopicSelectionFragment.this.activity.showFacebookSettingsFragment();
            }
        });

        Button startTopicButton = (Button) view.findViewById(R.id.topicSelectionNewTopicButton);
        startTopicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TopicSelectionFragment.this.activity.showStartTopicFragment();
            }
        });

        init(savedInstanceState);
        return view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data, nativeDialogCallback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean(PENDING_ANNOUNCE_KEY, pendingAnnounce);
        uiHelper.onSaveInstanceState(bundle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
        activity = null;
    }

    /**
     * Sets the user's values
     *
     * @param user
     *          - The user for whom the UI values will be set
     */
    public void setUserValues(User user) {
        // Set screen name
        TopicSelectionFragment.this.userScreenName.setText(user.getScreenName());

        // Display user's picture if required
        if (user.getPreferences().isShowProfilePicture()) {
            TopicSelectionFragment.this.profilePictureView.setProfileId(
                    String.valueOf(user.getFacebookProfileId()));
        } else {
            TopicSelectionFragment.this.profilePictureView.setProfileId(null);
        }
    }

    private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
        if (session != null && session.isOpened()) {
            if (state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
                // Do nothing
            } else {
                requestFacebookProfileInformation(session);
            }
        } else {
            profilePictureView.setProfileId(null);
        }
    }

    /**
     * Gets the Facebook profile ID from the Facebook session provided.
     *
     * @param session
     *              - The Facebook session to request the user's information from
     */
    private void requestFacebookProfileInformation(final Session session) {
        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (session == Session.getActiveSession()) {
                    if (user != null) {
                        TopicSelectionFragment.this.setFacebookProfileId(user.getId());
                    }
                }
                if (response.getError() != null) {
                    handleError(response.getError());
                }
            }
        });
        request.executeAsync();

    }

    /**
     * Loads the user for the Facebook profile ID provided in the background.
     *
     * @param facebookProfileId - The Facebook Profile ID of the user to load.
     */
    private void setFacebookProfileId(String facebookProfileId) {
        new GetISeekUserTask().execute(Long.valueOf(facebookProfileId));
    }

    /**
     * Gets the iSeek user based on the facebook profile ID provided. If the user doesn't already
     * exist, this task will create a user with default preferences and return the newly created
     * user.
     */
    private class GetISeekUserTask extends AsyncTask<Long, Void, User> {

        /**
         * {@inheritDoc}
         *
         * @param params
         *            - The Facebook profile ID to use in creating or loading the user from the
         *            persistent store.
         */
        @Override
        protected User doInBackground(Long... params) {
            // Get user service
            IUserService userService = ServicesFactory.getInstance().createUserService();

            // Call web service
            Location location = new Location(TopicSelectionFragment.this.activity.getLatitude(),
                    TopicSelectionFragment.this.activity.getLongitude());
            return userService.createUser(params[0], location);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(User user) {
            if (user != null) {
                Log.d("LOADED_USER", "userId=" + user.getId());
                TopicSelectionFragment.this.activity.setCurrentUser(user);

                // Set screen name
                TopicSelectionFragment.this.userScreenName.setText(user.getScreenName());

                // Display user's picture if required
                if (user.getPreferences().isShowProfilePicture()) {
                    TopicSelectionFragment.this.profilePictureView.setProfileId(
                            String.valueOf(user.getFacebookProfileId()));
                }

                // Load topics for user
                TopicSelectionFragment.this.loadTopicsForUsersLocation(user.getLastLocation());
            }
        }
    }

    /**
     * Loads the topics at the user's location
     *
     * @param location
     *              - The user's location
     */
    private void loadTopicsForUsersLocation(Location location) {
        new LoadTopicsTask().execute(location);
    }

    /**
     * Loads the topics at the location provided.
     */
    private class LoadTopicsTask extends AsyncTask<Location, Void, List<HashTag>> {

        /**
         * {@inheritDoc}
         *
         * @param params
         *            - The location to use in loading the topics being discussed
         */
        @Override
        protected List<HashTag> doInBackground(Location... params) {
            // Get topic service
            ITopicService topicService = ServicesFactory.getInstance().createTopicService();

            // Call web service
            return topicService.findTopics(params[0]);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(List<HashTag> topics) {
            if (topics != null) {
                Log.d("LOADED_TOPICS", "topics=" + topics);

                // Hide progress bar
                getActivity().findViewById(R.id.progressLayout).setVisibility(View.GONE);

                // Verify if there are any topics to display
                if (topics.isEmpty()) {
                    // Show no topics text
                    TopicSelectionFragment.this.activity.findViewById(
                            R.id.noTopicsLayout).setVisibility(View.VISIBLE);
                } else {
                    // Set user's topics
                    TopicSelectionFragment.this.activity.setUserTopics(topics);
                    TopicSelectionFragment.this.displayUserTopics(topics);
                }
            }
        }
    }

    /**
     * Displays the topics in a scrollable list with topic icons for each topic.
     *
     * @param topics
     *              - The topics to display.
     */
    public void displayUserTopics(List<HashTag> topics) {
        this.topicElements = new ArrayList<TopicListElement>();
        for (int i = 0; i < topics.size(); i++) {
            HashTag topic = topics.get(i);
            this.topicElements.add(new TopicListElement(topic));
        }
        listView.setAdapter(new TopicListElementArrayAdapter(
                getActivity(), R.id.topic_list, topicElements));
    }

    /**
     * Resets the view to the initial defaults.
     */
    private void init(Bundle savedInstanceState) {
        // Initialize list of topics
        this.topicElements = new ArrayList<TopicListElement>();

        // Get Facebook user information
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            this.requestFacebookProfileInformation(session);
        }
    }

    private void requestPublishPermissions(Session session) {
        if (session != null) {
            Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, PERMISSION)
                    // demonstrate how to set an audience for the publish permissions,
                    // if none are set, this defaults to FRIENDS
                    .setDefaultAudience(SessionDefaultAudience.FRIENDS)
                    .setRequestCode(REAUTH_ACTIVITY_CODE);
            session.requestNewPublishPermissions(newPermissionsRequest);
        }
    }

    private void showSuccessResponse(String postId) {
        String dialogBody;
        if (postId != null) {
            dialogBody = String.format(getString(R.string.result_dialog_text_with_id), postId);
        } else {
            dialogBody = getString(R.string.result_dialog_text_default);
        }
        showResultDialog(dialogBody);
    }

    private void showCancelResponse() {
        showResultDialog(getString(R.string.result_dialog_text_canceled));
    }

    private void showResultDialog(String dialogBody) {
        new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.result_dialog_button_text, null)
                .setTitle(R.string.result_dialog_title)
                .setMessage(dialogBody)
                .show();
    }

    private void handleError(FacebookRequestError error) {
        DialogInterface.OnClickListener listener = null;
        String dialogBody;

        if (error == null) {
            dialogBody = getString(R.string.error_dialog_default_text);
        } else {
            switch (error.getCategory()) {
                case AUTHENTICATION_RETRY:
                    // tell the user what happened by getting the message id, and
                    // retry the operation later
                    String userAction = (error.shouldNotifyUser()) ? "" :
                            getString(error.getUserActionMessageId());
                    dialogBody = getString(R.string.error_authentication_retry, userAction);
                    listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, M_FACEBOOK_URL);
                            startActivity(intent);
                        }
                    };
                    break;

                case AUTHENTICATION_REOPEN_SESSION:
                    // close the session and reopen it.
                    dialogBody = getString(R.string.error_authentication_reopen);
                    listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Session session = Session.getActiveSession();
                            if (session != null && !session.isClosed()) {
                                session.closeAndClearTokenInformation();
                            }
                        }
                    };
                    break;

                case PERMISSION:
                    // request the publish permission
                    dialogBody = getString(R.string.error_permission);
                    listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            pendingAnnounce = true;
                            requestPublishPermissions(Session.getActiveSession());
                        }
                    };
                    break;

                case SERVER:
                case THROTTLING:
                    // this is usually temporary, don't clear the fields, and
                    // ask the user to try again
                    dialogBody = getString(R.string.error_server);
                    break;

                case BAD_REQUEST:
                    // this is likely a coding error, ask the user to file a bug
                    dialogBody = getString(R.string.error_bad_request, error.getErrorMessage());
                    break;

                case OTHER:
                case CLIENT:
                default:
                    // an unknown issue occurred, this could be a code error, or
                    // a server side issue, log the issue, and either ask the
                    // user to retry, or file a bug
                    dialogBody = getString(R.string.error_unknown, error.getErrorMessage());
                    break;
            }
        }

        String title = error.getErrorUserTitle();
        String message = error.getErrorUserMessage();
        if (message == null) {
            message = dialogBody;
        }
        if (title == null) {
            title = getResources().getString(R.string.error_dialog_title);
        }

        new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.error_dialog_button_text, listener)
                .setTitle(title)
                .setMessage(message)
                .show();
    }
}
