package com.optimed.dto;

import com.optimed.entity.enums.Specialization;
import lombok.AllArgsConstructor;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.*;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppointmentRequest {
    private UUID doctorId;

    private UUID patientId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate appointmentDate;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime appointmentTime;

    private Specialization specialization;

    private String reason;

}
