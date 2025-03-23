package com.optimednotifications.event;

import lombok.*;
import org.springframework.context.ApplicationEvent;

@Getter
public class NotificationEvent extends ApplicationEvent {
    private final String email;
    private final String message;

    public NotificationEvent(Object source, String email, String message) {
        super(source);
        this.email = email;
        this.message = message;
    }
}