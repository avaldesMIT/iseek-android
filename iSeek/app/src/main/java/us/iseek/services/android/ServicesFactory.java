/**
 * Copyright (C) 2015 iSeek, Inc.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of iSeek, Inc.
 * You shall not disclose such confidential information and shall use it only
 * in accordance with the terms of the license agreement you entered into with
 * iSeek, Inc.
 */
package us.iseek.services.android;

import us.iseek.services.IMessageService;
import us.iseek.services.ITopicService;
import us.iseek.services.IUserService;
import us.iseek.services.android.rest.MessageServiceRestAdapter;
import us.iseek.services.android.rest.TopicServiceRestAdapter;
import us.iseek.services.android.rest.UserServiceRestAdapter;

/**
 * Creates instances of fully initialized services.
 *
 * @author Armando Valdes
 * @since 1.0
 */
public final class ServicesFactory {
    // Singleton instance of this
    private static ServicesFactory SINGLETON_INSTANCE;

    // Service instances
    private IUserService userService;
    private ITopicService topicService;
    private IMessageService messageService;

    // REST Adapter instances
    private UserServiceRestAdapter userServiceRestAdapter;
    private TopicServiceRestAdapter topicServiceRestAdapter;
    private MessageServiceRestAdapter messageServiceRestAdapter;

    /**
     * Creates a new instance of this
     */
    private ServicesFactory() {
        // Hide utility constructor
    }

    /**
     * Gets an instance of this.
     *
     * @return An instance of this factory.
     */
    public static ServicesFactory getInstance() {
        if (ServicesFactory.SINGLETON_INSTANCE == null) {
            ServicesFactory.SINGLETON_INSTANCE = new ServicesFactory();
        }
        return ServicesFactory.SINGLETON_INSTANCE;
    }

    /**
     * Creates an implementation of <tt>IUserService</tt> that delegates calls to a web service.
     * 
     * @return A full initialized <tt>IUserService</tt> implementation instance.
     */
    public IUserService createUserService() {
        if (this.userService == null) {
            this.userService = new UserService();
            ((UserService) this.userService).setRestAdapter(this.createUserServiceRestAdapter());
        }
        return this.userService;
    }

    /**
     * Creates an implementation of <tt>ITopicService</tt> that delegates calls to a web service.
     *
     * @return A full initialized <tt>ITopicService</tt> implementation instance.
     */
    public ITopicService createTopicService() {
        if (this.topicService == null) {
            this.topicService = new TopicService();
            ((TopicService) this.topicService).setRestAdapter(this.createTopicServiceRestAdapter());
        }
        return this.topicService;
    }

    /**
     * Creates an implementation of <tt>IMessageService</tt> that delegates calls to a web service.
     *
     * @return A full initialized <tt>IMessageService</tt> implementation instance.
     */
    public IMessageService createMessageService() {
        if (this.messageService == null) {
            this.messageService = new MessageService();
            ((MessageService) this.messageService).setRestAdapter(
                    this.createMessageServiceRestAdapter());
        }
        return this.messageService;
    }

    /**
     * Creates a singleton instance of <tt>UserServiceRestAdapter</tt>
     * 
     * @return The singleton instance of <tt>UserServiceRestAdapter</tt>
     */
    private UserServiceRestAdapter createUserServiceRestAdapter() {
        if (this.userServiceRestAdapter == null) {
            this.userServiceRestAdapter = new UserServiceRestAdapter();
        }
        return this.userServiceRestAdapter;
    }

    /**
     * Creates a singleton instance of <tt>TopicServiceRestAdapter</tt>
     *
     * @return The singleton instance of <tt>TopicServiceRestAdapter</tt>
     */
    private TopicServiceRestAdapter createTopicServiceRestAdapter() {
        if (this.topicServiceRestAdapter == null) {
            this.topicServiceRestAdapter = new TopicServiceRestAdapter();
        }
        return this.topicServiceRestAdapter;
    }

    /**
     * Creates a singleton instance of <tt>MessageServiceRestAdapter</tt>
     *
     * @return The singleton instance of <tt>MessageServiceRestAdapter</tt>
     */
    private MessageServiceRestAdapter createMessageServiceRestAdapter() {
        if (this.messageServiceRestAdapter == null) {
            this.messageServiceRestAdapter = new MessageServiceRestAdapter();
        }
        return this.messageServiceRestAdapter;
    }
}
