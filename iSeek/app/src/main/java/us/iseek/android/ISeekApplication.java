/**
 * Copyright (C) 2015 iSeek, Inc.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of iSeek, Inc.
 * You shall not disclose such confidential information and shall use it only
 * in accordance with the terms of the license agreement you entered into with
 * iSeek, Inc.
 */
package us.iseek.android;

import android.app.Application;
import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;

import java.util.List;

/**
 * Defines the iSeek application. Contains data related to all activities.
 *
 * @author Armando Valdes
 * @since 1.0
 */
public class ISeekApplication extends Application {

    private List<GraphUser> selectedUsers;

    /**
     * Gets the selected users
     *
     * @return the selectedUsers.
     */
    public List<GraphUser> getSelectedUsers() {
        return this.selectedUsers;
    }

    /**
     * Sets the selected users
     *
     * @param selectedUsers
     *                  - The selectedUsers to set.
     */
    public void setSelectedUsers(List<GraphUser> selectedUsers) {
        this.selectedUsers = selectedUsers;
    }
}
