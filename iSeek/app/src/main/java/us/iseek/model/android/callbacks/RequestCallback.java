/**
 * Copyright (C) 2015 iSeek, Inc.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of iSeek, Inc.
 * You shall not disclose such confidential information and shall use it only
 * in accordance with the terms of the license agreement you entered into with
 * iSeek, Inc.
 */
package us.iseek.model.android.callbacks;

import us.iseek.model.topics.Subscription;

/**
 * Provides request results for asynchronous method calls.
 *
 * @param <T>
 *          - The request's result type.
 * @author Armando Valdes
 * @since 1.0
 */
public interface RequestCallback<T> {

    /**
     * Handles the success of a request.
     *
     * @param result
     *              - The result of the request.
     */
    public void success(T result);
}
