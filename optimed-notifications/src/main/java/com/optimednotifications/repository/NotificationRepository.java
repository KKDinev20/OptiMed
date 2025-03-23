package com.optimednotifications.repository;

import com.optimednotifications.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByRecipientId (UUID recipientId);
}
