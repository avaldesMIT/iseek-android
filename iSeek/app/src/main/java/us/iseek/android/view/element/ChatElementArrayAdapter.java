/**
 * Copyright (C) 2015 iSeek, Inc.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of iSeek, Inc.
 * You shall not disclose such confidential information and shall use it only
 * in accordance with the terms of the license agreement you entered into with
 * iSeek, Inc.
 */
package us.iseek.android.view.element;

import android.content.Context;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import us.iseek.android.R;

/**
 * An array adapter for TopicListElements. Inflates the layout and sets the TopicListElement
 * on click listeners to the container.
 *
 * @author Armando Valdes
 * @since 1.0
 */
public class ChatElementArrayAdapter extends ArrayAdapter<ChatElement> {

    private Context context;
    private List<ChatElement> chatElements;

    /**
     * Creates a new instance of this.
     *
     * @param context {@inheritDoc}
     * @param resourceId {@inheritDoc}
     * @param listElements {@inheritDoc}
     */
    public ChatElementArrayAdapter(
            Context context, int resourceId, List<ChatElement> listElements) {

        super(context, resourceId, listElements);

        this.context = context;
        this.chatElements = listElements;
        for (ChatElement chatElement : this.chatElements) {
            chatElement.setAdapter(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // Inflate view if required
        if (view == null) {
            LayoutInflater inflater =
                    (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.chat_element, null);
        }

        // Get chat element
        ChatElement chatElement = this.chatElements.get(position);
        if (chatElement != null) {
            view.setOnClickListener(chatElement.getOnClickListener());

            // Set user name
            TextView userName
                    = (TextView) view.findViewById(R.id.chatElementUserName);
            userName.setText(chatElement.getUserName());

            // Set message text
            TextView messageText = (TextView) view.findViewById(R.id.chatElementMessageText);
            messageText.setText(chatElement.getMessageText());

            // Get message container
            LinearLayout messageContainer =
                    (LinearLayout) view.findViewById(R.id.chatElementMessageContainer);

            // Determine if this is a received message
            Resources resources = view.getResources();
            if (chatElement.isReceivedMessage()) {
                // Align received messages to the left and color appropriately
                userName.setGravity(Gravity.LEFT);
                messageContainer.setGravity(Gravity.LEFT);
                messageText.setBackgroundResource(R.drawable.message_received);
                messageText.setTextColor(resources.getColor(R.color.received_message_text));
            } else {
                // Align received messages to the right and color appropriately
                userName.setGravity(Gravity.RIGHT);
                messageContainer.setGravity(Gravity.RIGHT);
                messageText.setBackgroundResource(R.drawable.message_sent);
                messageText.setTextColor(resources.getColor(R.color.sent_message_text));
            }
        }
        return view;
    }
}
