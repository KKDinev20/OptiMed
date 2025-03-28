package com.optimed.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationRequest {
    private UUID id;
    private String email;
    private String message;
    private LocalDateTime createdAt;
    private boolean isRead;
}
