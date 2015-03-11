/**
 * Copyright (C) 2015 iSeek, Inc.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of iSeek, Inc.
 * You shall not disclose such confidential information and shall use it only
 * in accordance with the terms of the license agreement you entered into with
 * iSeek, Inc.
 */
package us.iseek.android.activity;

import us.iseek.model.android.enums.MenuItem;

/**
 * An activity that provides call backs to navigate to the menu item selected to the user.
 *
 * @author Armando Valdes
 * @since 1.0
 */
public interface MenuDisplayingActivity {

    /**
     * Call back to invoke when the user selects a different menu item.
     *
     * @param selectedItem
     *                  - The new topic_selection
     */
    public void onMenuSelectedCallback(MenuItem selectedItem);
}
