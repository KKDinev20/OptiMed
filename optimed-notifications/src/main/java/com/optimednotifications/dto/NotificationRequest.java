package com.optimednotifications.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class NotificationRequest {
    private String email;
    private String message;
    private LocalDateTime createdAt;
    private boolean isReceived;
    private String recipientType;
}

