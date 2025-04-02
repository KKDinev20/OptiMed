package com.optimed.dto;

import jakarta.validation.constraints.*;
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

    @Email
    private String email;

    private String message;
    private LocalDateTime createdAt;
    private boolean isRead;
}
