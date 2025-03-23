package com.optimednotifications.service;

import com.optimednotifications.entity.*;
import com.optimednotifications.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final RecipientRepository recipientRepository;

    public NotificationService(NotificationRepository notificationRepository, RecipientRepository recipientRepository) {
        this.notificationRepository = notificationRepository;
        this.recipientRepository = recipientRepository;
    }

    public Notification createNotification(String email, String message) {
        Recipient recipient = recipientRepository.findByEmail(email)
                .orElseGet(() -> {
                    Recipient newRecipient = new Recipient();
                    newRecipient.setEmail(email);
                    return recipientRepository.save(newRecipient);
                });

        Notification notification = Notification.builder()
                .recipient(recipient)
                .message(message)
                .createdAt(LocalDateTime.now())
                .isReceived (false)
                .build();

        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsForRecipient(UUID recipientId) {
        return notificationRepository.findByRecipientId(recipientId);
    }
}
