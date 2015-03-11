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
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import us.iseek.android.R;
import us.iseek.android.activity.MainActivity;
import us.iseek.android.view.element.ChatElement;
import us.iseek.android.view.element.ChatElementArrayAdapter;
import us.iseek.android.view.element.TopicListElementArrayAdapter;
import us.iseek.model.communication.PublicMessage;
import us.iseek.model.exception.UnsupportedMessageTypeException;
import us.iseek.model.topics.HashTag;
import us.iseek.model.user.User;
import us.iseek.services.IMessageService;
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

    private ListView chatHistoryList;
    private ScrollView chatScrollView;
    private List<ChatElement> chatMessages;

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
        this.chatScrollView = (ScrollView) view.findViewById(R.id.chatScrollView);
        this.chatHistoryList = (ListView) view.findViewById(R.id.chatHistoryList);

        this.chatMessages = new ArrayList<ChatElement>();

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
                ChatFragment.this.sendMessage();
            }
        });

        // Return newly created view
        return view;
    }

    /**
     * Processes the send message action
     */
    private void sendMessage() {
        // Clear text field focus and hide keyboard
        this.chatText.clearFocus();
        this.hideKeyword();

        this.chatScrollView.post(new Runnable() {
            @Override
            public void run() {
                ChatFragment.this.chatScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });

        // Get chat text
        String message = this.chatText.getText().toString();

        // Clear chat field
        this.chatText.setText(getResources().getString(R.string.default_chat_text));
        this.chatText.setTextColor(getResources().getColor(R.color.text_disabled));

        // Send message
        new SendMessageTask().execute(message);

        // Show message
        this.displayMessage(message);
    }

    /**
     * Displays the message provided on the screen.
     *
     * @param message
     *              - The message to display
     */
    private void displayMessage(String message) {
        // Add message to list
        this.chatMessages.add(new ChatElement(
                this.activity.getCurrentUser().getScreenName(),
                message, false));

        // TODO: Remove code for testing
        this.chatMessages.add(new ChatElement(
                "Respondent",
                "Really?", true));

        // Draw message to end
        this.chatHistoryList.setAdapter(new ChatElementArrayAdapter(
                getActivity(), R.id.chatHistoryList, this.chatMessages));
    }

    /**
     * Displays the topic name
     *
     * @param topic
     *              - The topic to display
     */
    public void displayTopicName(HashTag topic) {
        this.topicName.setText(topic.getDisplayName());
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
