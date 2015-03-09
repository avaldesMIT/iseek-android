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
 * Represents a topic list element, consisting of a topic icon to the left, and a two line display
 * to the right. The first line in the display corresponds to the topic's display name. The
 * second line is reserved for descriptive information about the topic.
 *
 * @author Armando Valdes
 * @since 1.0
 */
public class TopicListElement {

    // Drawing adapter
    private BaseAdapter adapter;
    View.OnClickListener onClickListener;

    // Topic properties
    private HashTag topic;
    private String displayName;
    private String description;

    /**
     * Creates a new instance of this.
     *
     * @param topic
     *              - This element's topic
     */
    public TopicListElement(HashTag topic) {
        // Set topic attributes
        this.topic = topic;
        this.displayName = topic.getDisplayName();
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
     * Returns the first row of text.
     *
     * @return the first row of text
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the second row of text.
     *
     * @return the second row of text
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the first row of text.
     *
     * @param displayName text to set on the first row
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Sets the second row of text.
     *
     * @param description text to set on the second row
     */
    public void setDescription(String description) {
        this.description = description;
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
