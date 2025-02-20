package com.optimed.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID appointmentId;

    @Column(nullable = false)
    private LocalDateTime reminderDate;

    @Column(nullable = false)
    private Boolean sentStatus;
}
