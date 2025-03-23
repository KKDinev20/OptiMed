package com.optimednotifications.service;

import com.optimednotifications.event.NotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class NotificationEventPublisher {
    private final ApplicationEventPublisher publisher;

    public NotificationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishNotificationEvent(String email, String message) {
        publisher.publishEvent(new NotificationEvent (this, email, message));
    }
}