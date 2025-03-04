package com.optimed.dto;

import com.optimed.entity.*;
import com.optimed.entity.enums.AppointmentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.*;

@Data
public class AppointmentRequest {
    private UUID id;

    private DoctorProfile doctor;

    private PatientProfile patient;

    private LocalDateTime appointmentDate;

    private AppointmentStatus status;
}
