/**
 * Copyright (C) 2015 iSeek, Inc. 
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of iSeek, Inc.
 * You shall not disclose such confidential information and shall use it only 
 * in accordance with the terms of the license agreement you entered into with 
 * iSeek, Inc.
 */
package us.iseek.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import us.iseek.android.R;
import us.iseek.android.activity.MenuDisplayingActivity;
import us.iseek.model.android.MenuItem;

/**
 * A custom menu to be used across all fragments.
 *
 * @author Armando Valdes
 * @since 1.0
 */
public class MenuView extends LinearLayout {

    private static final int MAX_MENU_ELEMENTS = 4;

    private MenuDisplayingActivity activity;

    /**
     * Creates a new instance of this
     *
     * @param context
     *              - The view's context
     */
    public MenuView(Context context) {
        this(context, null);
    }

    /**
     * Creates a new instance of this
     *
     * @param context
     *              - The view's context
     * @param attrs
     *              - The view attributes
     */
    public MenuView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Host activity is passed in context
        this.activity = (MenuDisplayingActivity) context;

        // Get selected element from menu's style
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.MenuView, 0, 0);
        String selectedItem = attributes.getString(R.styleable.MenuView_selectedItem);
        attributes.recycle();

        // Define selected option
        MenuItem selectedMenu = MenuItem.fromSymbol(selectedItem);
        if (selectedMenu == null) {
            // Default to topic selection
            selectedMenu = MenuItem.TOPICS_BUTTON;
        }

        // Set view options
        this.setOrientation(LinearLayout.HORIZONTAL);
        this.setGravity(Gravity.CENTER_VERTICAL);

        // Inflate menu items
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.menu_view, this, true);

        // Set selected menu item
        this.showSelectedMenuItem(selectedMenu);

        // Add call backs to menu items
        this.addOnClickListeners(selectedMenu);
    }

    /**
     * Sets the selected menu item. This view consists of images of all selected menu items
     * and images of all unselected menu items. This method hides the unselected image of the
     * selectedMenu and the selected images of all other menu items.
     *
     * @param selectedMenu
     *                  - The selected menu item.
     */
    private void showSelectedMenuItem(MenuItem selectedMenu) {
        // Set unselected options
        for (int i = 0; i < (MAX_MENU_ELEMENTS * 2); i += 2) {
            // Get unselected button image
            ImageView button = (ImageView) this.getChildAt(i);

            // If this button is selected, hide unselected image
            if (i == selectedMenu.getUnselectedIndex()) {
                button.setVisibility(GONE);
            }
        }

        // Set selected options
        for (int i = 1; i < (MAX_MENU_ELEMENTS * 2); i += 2) {
            // Get selected button image
            ImageView button = (ImageView) this.getChildAt(i);

            // If this button is not selected, hide unselected image
            if (i != selectedMenu.getSelectedIndex()) {
                button.setVisibility(GONE);
            }
        }
    }

    /**
     * Adds on click listeners to all buttons (except for the current selected menu item) to call
     * the hosting activity when the user selects a new menu item.
     *
     * @param selectedMenu
     *              - The current selected menu item.
     */
    private void addOnClickListeners(MenuItem selectedMenu) {
        // Add on click listeners
        for (int i = 1; i < (MAX_MENU_ELEMENTS * 2); i += 2) {
            // Get selected and unselected button images
            ImageView selectedButton = (ImageView) this.getChildAt(i);
            ImageView unSelectedButton = (ImageView) this.getChildAt(i - 1);

            // Only add listeners to unselected menu items
            if (i != selectedMenu.getSelectedIndex()) {
                final MenuItem menuItem = MenuItem.fromSelectedIndex(i);
                selectedButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MenuView.this.activity.onMenuSelectedCallback(menuItem);
                    }
                });
                unSelectedButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MenuView.this.activity.onMenuSelectedCallback(menuItem);
                    }
                });
            }
        }
    }
}
