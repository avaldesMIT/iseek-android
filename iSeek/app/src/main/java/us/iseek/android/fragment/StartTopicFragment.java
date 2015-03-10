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

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import us.iseek.android.R;
import us.iseek.android.activity.MainActivity;
import us.iseek.model.exception.UnknownLocationException;
import us.iseek.model.topics.HashTag;
import us.iseek.model.topics.Subscription;
import us.iseek.services.ITopicService;
import us.iseek.services.android.ServicesFactory;


/**
 * A fragment that allows the user to start a new topic of conversation.
 *
 * @author Armando Valdes
 * @since 1.0
 */
public class StartTopicFragment extends Fragment {
    private MainActivity activity;

    private EditText topicName;
    private TextView topicError;

    private Button startTopicButton;
    private TextView startTopicProgressLabel;
    private ProgressBar startTopicProgressBar;
    private LinearLayout startTopicButtonBorder;

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
        View view = inflater.inflate(R.layout.start_topic, container, false);

        // Get references for UI elements
        this.topicName = (EditText) view.findViewById(R.id.topicName);
        this.topicError = (TextView) view.findViewById(R.id.topicErrorLabel);
        this.startTopicButton = (Button) view.findViewById(R.id.createTopicButton);
        this.startTopicProgressBar = (ProgressBar) view.findViewById(R.id.startTopicProgressBar);
        this.startTopicProgressLabel = (TextView) view.findViewById(R.id.startTopicProgressLabel);
        this.startTopicButtonBorder = (LinearLayout) view.findViewById(R.id.createTopicButtonBorder);

        // Set topic name listeners
        this.topicName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // Clear text tip when user selects field
                if (hasFocus && getResources().getString(R.string.default_topic_name).equals(
                        StartTopicFragment.this.topicName.getText().toString())) {

                    StartTopicFragment.this.topicName.setText("");
                    StartTopicFragment.this.topicName.setTextColor(getResources().getColor(R.color.text));
                }
            }
        });

        // Set button listeners
        this.startTopicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear focus
                StartTopicFragment.this.topicName.clearFocus();
                StartTopicFragment.this.hideKeyword();

                // Get topic text
                String newTopic = StartTopicFragment.this.topicName.getText().toString();

                // Verify topic
                if (newTopic.isEmpty()) {
                    StartTopicFragment.this.topicError.setVisibility(View.VISIBLE);
                } else {
                    StartTopicFragment.this.createTopic(newTopic);
                }
            }
        });

        // Return newly created view
        return view;
    }

    /**
     * Hides the keyword
     */
    private void hideKeyword() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) this.activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                this.activity.getCurrentFocus().getWindowToken(), 0);
    }

    /**
     * Creates a new topic with the topic name provided.
     *
     * @param topicName
     *              - The name of the topic to create
     */
    private void createTopic(String topicName) {
        // Hide errors and button (to prevent double click)
        this.topicError.setVisibility(View.GONE);
        this.startTopicButton.setVisibility(View.GONE);
        this.startTopicButtonBorder.setVisibility(View.GONE);

        // Show progress information
        this.startTopicProgressBar.setVisibility(View.VISIBLE);
        this.startTopicProgressLabel.setVisibility(View.VISIBLE);

        // Set progress description
        this.startTopicProgressLabel.setText(
                getResources().getString(R.string.create_topic_progress_bar_text));

        // Create topic
        new CreateTopicTask().execute(topicName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();

        // Reset UI elements
        this.resetView();
    }

    /**
     * Resets the view UI elements.
     */
    private void resetView() {
        // Set default text and color
        this.topicName.setText(getResources().getString(R.string.default_topic_name));
        this.topicName.setTextColor(getResources().getColor(R.color.text_disabled));

        // Show start topic button
        this.startTopicButton.setVisibility(View.VISIBLE);
        this.startTopicButtonBorder.setVisibility(View.VISIBLE);

        // Hide progress bar
        this.startTopicProgressBar.setVisibility(View.GONE);
        this.startTopicProgressLabel.setVisibility(View.GONE);
    }

    /**
     * Creates a new topic.
     */
    private class CreateTopicTask extends AsyncTask<String, Void, HashTag> {

        /**
         * {@inheritDoc}
         *
         * @param params
         *            - The name of the topic to create.
         */
        @Override
        protected HashTag doInBackground(String... params) {
            // Get topic service
            ITopicService topicService = ServicesFactory.getInstance().createTopicService();

            // Call web service
            String topicName = params[0];
            return topicService.createTopic(topicName);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(HashTag topic) {
            if (topic != null) {
                // Add topic to list
                List<HashTag> userTopics = StartTopicFragment.this.activity.getUserTopics();
                if (userTopics == null) {
                    userTopics = new ArrayList<HashTag>();
                    StartTopicFragment.this.activity.setUserTopics(userTopics);
                }
                userTopics.add(topic);

                // Automatically subscribe user to topic
                StartTopicFragment.this.subscribeToTopic(topic);
            }
        }
    }

    /**
     * Subscribes the activity.currentUser to the topic provided.
     *
     * @param topic
     *          - The topic to subscribe the currentUser to.
     */
    private void subscribeToTopic(HashTag topic) {
        // Set progress description
        this.startTopicProgressLabel.setText(
                getResources().getString(R.string.subscribe_topic_progress_bar_text));

        // Subscribe to topic
        new SubscribeToTopicTask().execute(topic);
    }

    /**
     * Subscribes the current user to the topic provided
     */
    private class SubscribeToTopicTask extends AsyncTask<HashTag, Void, Subscription> {

        /**
         * {@inheritDoc}
         *
         * @param params
         *            - The topic to subscribe to.
         */
        @Override
        protected Subscription doInBackground(HashTag... params) {
            // Get topic service
            ITopicService topicService = ServicesFactory.getInstance().createTopicService();

            // Call web service
            HashTag topic = params[0];
            try {
                return topicService.subscribe(
                        StartTopicFragment.this.activity.getCurrentUser().getId(), topic);
            } catch (UnknownLocationException e) {
                Log.e("SUBSCRIPTION_ERROR", "Could not determine user's location", e);
                return null;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(Subscription subscription) {
            // Reset UI elements
            StartTopicFragment.this.resetView();

            // Show newly created topic
            StartTopicFragment.this.activity.setSelectedTopic(subscription.getTopic());
        }
    }
}
