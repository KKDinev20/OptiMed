package com.optimednotifications.listener;

import com.optimednotifications.event.NotificationEvent;
import com.optimednotifications.service.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {
    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        notificationService.createNotification(event.getEmail(), event.getMessage());
    }
}