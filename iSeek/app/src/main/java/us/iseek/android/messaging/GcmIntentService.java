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

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import us.iseek.model.communication.MessageAbstract;

/**
 * An intent service that does the processing of GCM messages and releases the wake
 * lock acquired by the broadcast receiver.
 *
 * @author Armando Valdes
 * @since 1.0
 */
public class GcmIntentService extends IntentService {

    private static final String WORKER_THREAD_NAME = "GcmIntentServiceWorkerThread";

    /**
     * Creates a new instance of this.
     */
    public GcmIntentService() {
        super(WORKER_THREAD_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve the message type from the intent
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        // Verify there is anything to process
        Bundle messageBundle = intent.getExtras();
        Log.d("RECEIVED_MESSAGE", "messageBundle=" + messageBundle);
        if (!messageBundle.isEmpty()
                && GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

            // Get actual message
            MessageAbstract message = new MessageAbstract();
            message.setId((Long) messageBundle.get("id"));
            message.setMessage((String) messageBundle.get("message"));
            message.setSenderUserId((Long) messageBundle.get("senderUserId"));
            message.setSenderScreenName((String) messageBundle.get("senderScreenName"));
            Log.d("PARSED_MESSAGE", "message=" + message);

            // Publish message
            MessageTopic.getInstance().publish(message);
        }

        // Release the wake lock
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
}
