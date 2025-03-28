package com.optimednotifications.service;

import com.optimednotifications.event.NotificationEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationEventPublisherTest {
    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private NotificationEventPublisher notificationEventPublisher;

    @Test
    void publishNotificationEvent_ShouldPublishEvent() {
        // Given
        String email = "test@example.com";
        String message = "Test message";

        // When
        notificationEventPublisher.publishNotificationEvent(email, message);

        // Then
        verify(publisher, times(1)).publishEvent(
                argThat(event ->
                        event instanceof NotificationEvent &&
                                ((NotificationEvent) event).getEmail().equals(email) &&
                                ((NotificationEvent) event).getMessage().equals(message)
                )
        );
    }

    @Test
    void publishNotificationEvent_WithNullEmail_ShouldPublishEvent() {
        // Given
        String email = null;
        String message = "Test message";

        // When
        notificationEventPublisher.publishNotificationEvent(email, message);

        // Then
        verify(publisher, times(1)).publishEvent(
                argThat(event ->
                        event instanceof NotificationEvent &&
                                ((NotificationEvent) event).getEmail() == null &&
                                ((NotificationEvent) event).getMessage().equals(message)
                )
        );
    }

    @Test
    void publishNotificationEvent_WithNullMessage_ShouldPublishEvent() {
        // Given
        String email = "test@example.com";
        String message = null;

        // When
        notificationEventPublisher.publishNotificationEvent(email, message);

        // Then
        verify(publisher, times(1)).publishEvent(
                argThat(event ->
                        event instanceof NotificationEvent &&
                                ((NotificationEvent) event).getEmail().equals(email) &&
                                ((NotificationEvent) event).getMessage() == null
                )
        );
    }

    @Test
    void publishNotificationEvent_WithNullBoth_ShouldPublishEvent() {
        // Given
        String email = null;
        String message = null;

        // When
        notificationEventPublisher.publishNotificationEvent(email, message);

        // Then
        verify(publisher, times(1)).publishEvent(
                argThat(event ->
                        event instanceof NotificationEvent &&
                                ((NotificationEvent) event).getEmail() == null &&
                                ((NotificationEvent) event).getMessage() == null
                )
        );
    }
}