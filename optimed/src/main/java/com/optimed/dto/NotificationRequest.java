package com.optimed.dto;

import lombok.*;

import java.util.UUID;

@Data
@Getter
@Setter
public class NotificationRequest {
    private UUID id;
    private String email;
    private String message;
    private boolean isRead;
}
