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
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;
import com.facebook.model.OpenGraphAction;

import us.iseek.android.R;
import us.iseek.model.topics.HashTag;

/**
 * Represents a chat message element.
 *
 * @author Armando Valdes
 * @since 1.0
 */
public class ChatElement {

    // Drawing adapter
    private BaseAdapter adapter;
    View.OnClickListener onClickListener;

    // Element properties
    private String userName;
    private String messageText;
    private boolean isReceivedMessage;

    /**
     * Creates a new instance of this.
     *
     * @param userName
     *              - This name of the user who sent the message
     * @param messageText
     *              - The text of the message
     * @param isReceivedMessage
     *              - Determines if this message was received from other user
     */
    public ChatElement(String userName, String messageText, boolean isReceivedMessage) {
        // Set element attributes
        this.userName = userName;
        this.messageText = messageText;
        this.isReceivedMessage = isReceivedMessage;
    }

    /**
     * The Adapter associated with this list element (used for notifying that the
     * underlying dataset has changed).
     * @param adapter the adapter associated with this element
     */
    public void setAdapter(BaseAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * Returns the name of the user who sent the message.
     *
     * @return the userName
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * Returns the text of the message.
     *
     * @return the messageText
     */
    public String getMessageText() {
        return this.messageText;
    }

    /**
     * Determines if this is a message the user received.
     *
     * @return true if and only if this is a message the user received.
     */
    public boolean isReceivedMessage() {
        return this.isReceivedMessage;
    }

    /**
     * Sets the name of the user who sent the message.
     *
     * @param userName
     *              - The userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
        if (this.adapter != null) {
            this.adapter.notifyDataSetChanged();
        }
    }

    /**
     * Sets the text of the message.
     *
     * @param messageText
     *              - The messageText to set
     */
    public void setMessageText(String messageText) {
        this.messageText = messageText;
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Sets the indicator of whether this message was received by the current user.
     *
     * @param isReceivedMessage
     *              - The isReceivedMessage to set
     */
    public void setIsReceivedMessage(boolean isReceivedMessage) {
        this.isReceivedMessage = isReceivedMessage;
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Sets the OnClickListener to this list item.
     *
     * @param onClickListener
     *                  - The onClickListener to set.
     */
    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    /**
     * Returns the OnClickListener associated with this list element.
     *
     * @return the OnClickListener.
     */
    public View.OnClickListener getOnClickListener() {
        return this.onClickListener;
    }

    /**
     * Notifies the associated Adapter that the underlying data has changed,
     * and to re-layout the view.
     */
    protected void notifyDataChanged() {
        adapter.notifyDataSetChanged();
    }
}
