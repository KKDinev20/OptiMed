package com.optimed.dto;

import com.optimed.entity.enums.AppointmentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ListAppointmentRequest {

    private UUID id;
    private UUID doctorId;
    private String doctorFullname;
    private UUID patientId;
    private String patientUsername;
    private LocalDateTime appointmentDate;
    private AppointmentStatus status;
}
