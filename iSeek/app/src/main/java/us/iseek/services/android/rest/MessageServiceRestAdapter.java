/**
 * Copyright (C) 2015 iSeek, Inc.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of iSeek, Inc.
 * You shall not disclose such confidential information and shall use it only
 * in accordance with the terms of the license agreement you entered into with
 * iSeek, Inc.
 */
package us.iseek.services.android.rest;

import android.util.Log;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import us.iseek.model.android.HttpException;
import us.iseek.model.communication.Message;
import us.iseek.model.communication.PrivateMessage;
import us.iseek.model.communication.PublicMessage;
import us.iseek.model.enums.MessageType;
import us.iseek.model.exception.UnsupportedMessageTypeException;
import us.iseek.model.topics.HashTag;
import us.iseek.services.IMessageService;

/**
 * Invokes a REST web service to send messages to other users or groups of users, or to
 * broadcast messages to a topic.
 *
 * @author Armando Valdes
 * @since 1.0
 */
public class MessageServiceRestAdapter implements IMessageService {

    private static final String MESSAGE_PATH = "/message";

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(Message message) throws UnsupportedMessageTypeException {
        if (MessageType.PUBLIC.equals(message.getType())) {
            this.sendPublicMessage((PublicMessage) message);
        } else if (MessageType.PRIVATE.equals(message.getType())) {
            this.sendPrivateMessage((PrivateMessage) message);
        } else {
            Log.e("UNSUPPORTED_MESSAGE_TYPE", "Could not send message=" + message);
        }
    }

    /**
     * Sends the private message according to the instructions specified in the message.
     *
     * @param privateMessage
     *            - The private message to send, along with the instructions on who to
     *            send it to.
     */
    private void sendPrivateMessage(PrivateMessage privateMessage) {
        // Define connection parameters
        Log.d("SEND_PRIVATE_MESSAGE", "message=" + privateMessage);
        String createUrl = RestConstants.BASE_URL + MESSAGE_PATH + "/private/send";

        try {
            // Call web service
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            restTemplate.postForObject(createUrl, privateMessage, Void.class);
        } catch (Exception e) {
            Log.e("HTTP_EXCEPTION", "Could not send private message", e);
            throw new HttpException("Could not send private message", e);
        }
    }

    /**
     * Sends the public message according to the instructions specified in the message.
     *
     * @param publicMessage
     *            - The public message to send, along with the instructions on who to
     *            send it to.
     */
    private void sendPublicMessage(PublicMessage publicMessage) {
        // Define connection parameters
        Log.d("SEND_PUBLIC_MESSAGE", "message=" + publicMessage);
        String createUrl = RestConstants.BASE_URL + MESSAGE_PATH + "/public/send";

        try {
            // Call web service
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            restTemplate.postForObject(createUrl, publicMessage, Void.class);
        } catch (Exception e) {
            Log.e("HTTP_EXCEPTION", "Could not send public message", e);
            throw new HttpException("Could not send public message", e);
        }
    }
}
