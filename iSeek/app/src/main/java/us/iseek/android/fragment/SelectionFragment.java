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

package us.iseek.android.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.model.OpenGraphAction;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.ProfilePictureView;

import java.util.ArrayList;
import java.util.List;

import us.iseek.android.BaseListElement;
import us.iseek.android.activity.MainActivity;
import us.iseek.android.R;
import us.iseek.model.gps.Location;
import us.iseek.model.topics.HashTag;
import us.iseek.model.user.User;
import us.iseek.services.ITopicService;
import us.iseek.services.IUserService;
import us.iseek.services.android.ServicesFactory;

/**
 * A fragment for topic selection.
 */
public class SelectionFragment extends Fragment {

    private static final String PENDING_ANNOUNCE_KEY = "pendingAnnounce";
    private static final Uri M_FACEBOOK_URL = Uri.parse("http://m.facebook.com");

    private static final int REAUTH_ACTIVITY_CODE = 100;
    private static final String PERMISSION = "publish_actions";

    private ListView listView;
    private List<BaseListElement> topicElements;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        uiHelper = new UiLifecycleHelper(getActivity(), sessionCallback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.selection, container, false);

        this.listView = (ListView) view.findViewById(R.id.topic_list);
        this.userScreenName = (TextView) view.findViewById(R.id.userScreenName);
        this.profilePictureView = (ProfilePictureView) view.findViewById(R.id.selection_profile_pic);

        // Configure Facebook profile picture view
        this.profilePictureView.setCropped(true);
        this.profilePictureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectionFragment.this.activity.showFacebookSettingsFragment();
            }
        });

        init(savedInstanceState);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode >= 0 && requestCode < topicElements.size()) {
            topicElements.get(requestCode).onActivityResult(data);
        } else {
            uiHelper.onActivityResult(requestCode, resultCode, data, nativeDialogCallback);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        for (BaseListElement listElement : topicElements) {
            listElement.onSaveInstanceState(bundle);
        }
        bundle.putBoolean(PENDING_ANNOUNCE_KEY, pendingAnnounce);
        uiHelper.onSaveInstanceState(bundle);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

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
        SelectionFragment.this.userScreenName.setText(user.getScreenName());

        // Display user's picture if required
        if (user.getPreferences().isShowProfilePicture()) {
            SelectionFragment.this.profilePictureView.setProfileId(
                    String.valueOf(user.getFacebookProfileId()));
        } else {
            SelectionFragment.this.profilePictureView.setProfileId(null);
        }
    }

    private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
        if (session != null && session.isOpened()) {
            if (state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
                // Do nothing
            } else {
                makeMeRequest(session);
            }
        } else {
            profilePictureView.setProfileId(null);
        }
    }

    private void makeMeRequest(final Session session) {
        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (session == Session.getActiveSession()) {
                    if (user != null) {
                        SelectionFragment.this.setFacebookProfileId(user.getId());
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
            Location location = new Location(SelectionFragment.this.activity.getLatitude(),
                    SelectionFragment.this.activity.getLongitude());
            return userService.createUser(params[0], location);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(User user) {
            if (user != null) {
                SelectionFragment.this.activity.setCurrentUser(user);
                Toast.makeText(SelectionFragment.this.activity,
                        "Loaded user: " + user.getId(), Toast.LENGTH_LONG).show();

                // Set screen name
                SelectionFragment.this.userScreenName.setText(user.getScreenName());

                // Display user's picture if required
                if (user.getPreferences().isShowProfilePicture()) {
                    SelectionFragment.this.profilePictureView.setProfileId(
                            String.valueOf(user.getFacebookProfileId()));
                }

                // Load topics for user
                SelectionFragment.this.loadTopicsForUsersLocation(user.getLastLocation());
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
                Toast.makeText(getActivity(),
                        "Loaded topics: " + topics, Toast.LENGTH_LONG).show();

                // Hide progress bar
                getActivity().findViewById(R.id.progressLayout).setVisibility(View.GONE);

                // Add topics to list
                for (int i = 0; i < topics.size(); i++) {
                    HashTag topic = topics.get(i);
                    SelectionFragment.this.topicElements.add(new PeopleListElement(i, topic));
                }
                listView.setAdapter(new ActionListAdapter(
                        getActivity(), R.id.topic_list, topicElements));
            }
        }
    }

    /**
     * Resets the view to the initial defaults.
     */
    private void init(Bundle savedInstanceState) {
        // Initialize list of topics
        this.topicElements = new ArrayList<BaseListElement>();

        // Get Facebook user information
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            this.makeMeRequest(session);
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


    private class PeopleListElement extends BaseListElement {

        private HashTag topic;

        /**
         * Creates a new instance of this.
         *
         * @param requestCode
         *              - The request code to map this element to
         * @param topic
         *              - This element's topic
         */
        public PeopleListElement(int requestCode, HashTag topic) {
            super(getActivity().getResources().getDrawable(R.drawable.topic),
                    topic.getDisplayName(),
                    null,
                    requestCode);

            // Set topic
            this.topic = topic;
        }

        @Override
        public View.OnClickListener getOnClickListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Enter topic
                }
            };
        }

        @Override
        public void onActivityResult(Intent data) {
            // Don't need to do anything
        }

        @Override
        protected void populateOGAction(OpenGraphAction action) {
            // Don't need to do anything
        }

        @Override
        public void onSaveInstanceState(Bundle bundle) {
            // Don't need to do anything
        }

        @Override
        protected boolean restoreState(Bundle savedState) {
            // Don't need to do anything
            return false;
        }
    }

    private class ActionListAdapter extends ArrayAdapter<BaseListElement> {
        private List<BaseListElement> listElements;

        public ActionListAdapter(Context context, int resourceId, List<BaseListElement> listElements) {
            super(context, resourceId, listElements);
            this.listElements = listElements;
            for (int i = 0; i < listElements.size(); i++) {
                listElements.get(i).setAdapter(this);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater =
                        (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.listitem, null);
            }

            BaseListElement listElement = listElements.get(position);
            if (listElement != null) {
                view.setOnClickListener(listElement.getOnClickListener());
                ImageView icon = (ImageView) view.findViewById(R.id.icon);
                TextView text1 = (TextView) view.findViewById(R.id.text1);
                TextView text2 = (TextView) view.findViewById(R.id.text2);
                if (icon != null) {
                    icon.setImageDrawable(listElement.getIcon());
                }
                if (text1 != null) {
                    text1.setText(listElement.getText1());
                }
                if (text2 != null) {
                    if (listElement.getText2() != null) {
                        text2.setVisibility(View.VISIBLE);
                        text2.setText(listElement.getText2());
                    } else {
                        text2.setVisibility(View.GONE);
                    }
                }
            }
            return view;
        }

    }
}
