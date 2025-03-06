package com.optimed.dto;

import com.optimed.entity.enums.Specialization;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AppointmentRequest {
    private UUID doctorId;
    private UUID patientId;
    private LocalDateTime appointmentDate;
    private Specialization specialization;
    private String reason;
}
