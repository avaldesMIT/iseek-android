/**
 * Copyright (C) 2015 iSeek, Inc.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of iSeek, Inc.
 * You shall not disclose such confidential information and shall use it only
 * in accordance with the terms of the license agreement you entered into with
 * iSeek, Inc.
 */
package us.iseek.android.messaging;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * A receiver for GCM broadcasts that delegates message processing to an intent service. This
 * implementation keeps the device awake while the intent service processes the message. <br><br>
 *
 * <b>Note:</b> The intent service should release the wake lock when it is done with message
 * processing by calling GcmBroadcastReceiver.completeWakefulIntent.
 *
 * @author Armando Valdes
 * @since 1.0
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // Delegate processing to intent service
        ComponentName componentName = new ComponentName(context.getPackageName(),
                GcmIntentService.class.getName());

        // Acquire wake lock to ensure that device does not go back to sleep
        GcmBroadcastReceiver.startWakefulService(context, (intent.setComponent(componentName)));
        this.setResultCode(Activity.RESULT_OK);
    }
}
