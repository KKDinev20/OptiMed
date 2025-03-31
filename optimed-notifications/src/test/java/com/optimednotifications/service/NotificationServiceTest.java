package com.optimednotifications.service;

import com.optimednotifications.entity.*;
import com.optimednotifications.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.*;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NotificationServiceTest {
    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private RecipientRepository recipientRepository;

    @InjectMocks
    private NotificationService notificationService;

    private UUID existingRecipientId = UUID.randomUUID();
    private UUID newRecipientId = UUID.randomUUID();
    private UUID notificationId = UUID.randomUUID();

    private Recipient existingRecipient;
    private Recipient newRecipient;
    private Notification notification;

    @BeforeEach
    void setup() {
        existingRecipient = new Recipient();
        existingRecipient.setId(existingRecipientId);
        existingRecipient.setEmail("existing@example.com");

        newRecipient = new Recipient();
        newRecipient.setId(newRecipientId);
        newRecipient.setEmail("new@example.com");

        notification = Notification.builder()
                .id(notificationId)
                .recipient(existingRecipient)
                .message("Test message")
                .createdAt(LocalDateTime.now())
                .isReceived(false)
                .build();

        when(recipientRepository.findByEmail(eq("existing@example.com")))
                .thenReturn(Optional.of(existingRecipient));

        when(recipientRepository.save(any(Recipient.class)))
                .thenAnswer(invocation -> {
                    Recipient r = invocation.getArgument(0);
                    r.setId(newRecipientId);
                    return r;
                });

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> {
                    Notification n = invocation.getArgument(0);
                    n.setId(notificationId);
                    return n;
                });
    }

    @Test
    void createNotification_ExistingRecipient_ShouldSaveNotification() {
        // Given
        String email = "existing@example.com";
        String message = "Test message";

        // When
        Notification result = notificationService.createNotification(email, message);

        // Then
        assertEquals(message, result.getMessage());
        assertEquals(existingRecipientId, result.getRecipient().getId());
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(recipientRepository, never()).save(any(Recipient.class));
    }

    @Test
    void createNotification_NewRecipient_ShouldCreateRecipientAndNotification() {
        // Given
        String email = "new@example.com";
        String message = "Test message";
        when(recipientRepository.findByEmail(eq(email))).thenReturn(Optional.empty());

        // When
        Notification result = notificationService.createNotification(email, message);

        // Then
        assertEquals(message, result.getMessage());
        assertEquals(newRecipientId, result.getRecipient().getId());
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(recipientRepository, times(1)).save(any(Recipient.class));
    }

    @Test
    void getNotificationsForRecipient_ExistingRecipient_ShouldReturnNotifications() {
        // Given
        String email = "existing@example.com";
        when(notificationRepository.findByRecipientId(eq(existingRecipientId)))
                .thenReturn(Collections.singletonList(notification));

        // When
        List<Notification> notifications = notificationService.getNotificationsForRecipient(email);

        // Then
        assertFalse(notifications.isEmpty());
        assertEquals(1, notifications.size());
        assertEquals(notificationId, notifications.get(0).getId());
        assertEquals(existingRecipientId, notifications.get(0).getRecipient().getId());
        verify(recipientRepository, never()).save(any(Recipient.class));
    }

    @Test
    void createNotification_NewRecipient_OrElseGetShouldCreateRecipient() {
        // Given
        String email = "new@example.com";
        String message = "Test message";

        when(recipientRepository.findByEmail(eq(email))).thenReturn(Optional.empty());

        // When
        Notification result = notificationService.createNotification(email, message);

        // Then
        assertEquals(message, result.getMessage());
        assertEquals(email, result.getRecipient().getEmail());

        verify(recipientRepository, times(1)).findByEmail(eq(email));
        verify(recipientRepository, times(1)).save(any(Recipient.class));
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void ensureRecipientExists_NonExistentRecipient_ShouldCreateRecipient() {
        // Given
        String email = "nonexistent@example.com";
        when(recipientRepository.findByEmail(eq(email))).thenReturn(Optional.empty());

        // When
        notificationService.ensureRecipientExists(email);

        // Then
        verify(recipientRepository, times(1)).findByEmail(eq(email));
        verify(recipientRepository, times(1)).save(any(Recipient.class));
    }

    @Test
    void getNotificationsForRecipient_NewRecipient_ShouldCreateAndReturnEmptyList() {
        // Given
        String email = "new@example.com";
        when(recipientRepository.findByEmail(eq(email))).thenReturn(Optional.empty());
        when(notificationRepository.findByRecipientId(eq(newRecipientId)))
                .thenReturn(Collections.emptyList());

        // When
        List<Notification> notifications = notificationService.getNotificationsForRecipient(email);

        // Then
        assertTrue(notifications.isEmpty());
        verify(recipientRepository, times(1)).findByEmail(eq(email));
        verify(recipientRepository, times(1)).save(any(Recipient.class));
        verify(notificationRepository, times(1)).findByRecipientId(eq(newRecipientId));
    }

}