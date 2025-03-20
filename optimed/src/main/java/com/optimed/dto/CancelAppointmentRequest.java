package com.optimed.dto;

import lombok.*;

import java.time.*;

@Data
@AllArgsConstructor
public class CancelAppointmentRequest {
    private int canceledAppointments;
    private LocalTime executedAt;
}
