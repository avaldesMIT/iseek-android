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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import us.iseek.android.R;
import us.iseek.android.activity.MainActivity;
import us.iseek.model.communication.PublicMessage;
import us.iseek.model.exception.UnsupportedMessageTypeException;
import us.iseek.model.topics.HashTag;
import us.iseek.model.user.User;
import us.iseek.services.IMessageService;
import us.iseek.services.ITopicService;
import us.iseek.services.android.ServicesFactory;


/**
 * A fragment that provides communication options.
 *
 * @author Armando Valdes
 * @since 1.0
 */
public class ChatFragment extends Fragment {


    private MainActivity activity;

    private Button sendButton;
    private EditText chatText;
    private TextView topicName;
    private TextView chatHistory;

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
        View view = inflater.inflate(R.layout.chat, container, false);

        // Get references for UI elements
        this.chatText = (EditText) view.findViewById(R.id.chatText);
        this.topicName = (TextView) view.findViewById(R.id.chatTopicName);
        this.sendButton = (Button) view.findViewById(R.id.sendButton);
        this.chatHistory = (TextView) view.findViewById(R.id.chatHistory);

        // Set topic name listeners
        this.chatText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // Clear text tip when user selects field
                if (hasFocus && getResources().getString(R.string.default_chat_text).equals(
                        ChatFragment.this.chatText.getText().toString())) {

                    ChatFragment.this.chatText.setText("");
                    ChatFragment.this.chatText.setTextColor(getResources().getColor(R.color.text));
                }
            }
        });

        // Set button listeners
        this.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get chat text
                String newTopic = ChatFragment.this.chatText.getText().toString();

                // Clear chat field
                ChatFragment.this.chatText.setText(
                        getResources().getString(R.string.default_chat_text));
                ChatFragment.this.chatText.setTextColor(
                        getResources().getColor(R.color.text_disabled));

                // Send message
                new SendMessageTask().execute(newTopic);
            }
        });

        // Return newly created view
        return view;
    }

    /**
     * Sends a message.
     */
    private class SendMessageTask extends AsyncTask<String, Void, Void> {

        /**
         * {@inheritDoc}
         *
         * @param params
         *            - The name of the topic to create.
         */
        @Override
        protected Void doInBackground(String... params) {
            // Get message attributes
            String messageText = params[0];
            User user = ChatFragment.this.activity.getCurrentUser();
            HashTag topic = ChatFragment.this.activity.getSelectedTopic();

            Log.d("SEND_MESSAGE", "message_text=" + messageText + ", user=" + user + ", topic=" + topic);

            // Create message
            PublicMessage message = new PublicMessage();
            message.setMessage(messageText);
            message.setOrigin(user);
            message.setLocation(user.getLastLocation());
            message.setTopic(topic);

            // Get message service
            IMessageService messageService = ServicesFactory.getInstance().createMessageService();

            // Call web service
            try {
                messageService.sendMessage(message);
            } catch (UnsupportedMessageTypeException e) {
                Log.e("ERROR_SENDING_MESSAGE", "Error sending message", e);
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(Void params) {
            // Do nothing
        }
    }
}
