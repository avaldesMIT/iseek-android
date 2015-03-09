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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import us.iseek.android.R;

/**
 * An array adapter for TopicListElements. Inflates the layout and provides
 *
 * @author Armando Valdes
 * @since 1.0
 */
public class TopicListElementArrayAdapter extends ArrayAdapter<TopicListElement> {

    private Context context;
    private List<TopicListElement> listElements;

    /**
     * Creates a new instance of this.
     *
     * @param context {@inheritDoc}
     * @param resourceId {@inheritDoc}
     * @param listElements {@inheritDoc}
     */
    public TopicListElementArrayAdapter(
            Context context, int resourceId, List<TopicListElement> listElements) {

        super(context, resourceId, listElements);

        this.context = context;
        this.listElements = listElements;
        for (int i = 0; i < listElements.size(); i++) {
            listElements.get(i).setAdapter(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater =
                    (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.topic_list_item, null);
        }

        TopicListElement listElement = this.listElements.get(position);
        if (listElement != null) {
            view.setOnClickListener(listElement.getOnClickListener());
            TextView displayName
                    = (TextView) view.findViewById(R.id.topicListItemDisplayName);
            displayName.setText(listElement.getDisplayName());

            TextView description = (TextView) view.findViewById(R.id.topicListItemDescription);
            if (listElement.getDescription() != null) {
                description.setVisibility(View.VISIBLE);
                description.setText(listElement.getDescription());
            } else {
                description.setVisibility(View.GONE);
            }
        }
        return view;
    }
}
