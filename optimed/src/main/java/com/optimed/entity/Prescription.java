package com.optimed.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private DoctorProfile doctor;

    @ManyToOne
    private PatientProfile patient;

    private String medicationDetails;
    private String dosageInstructions;
    private LocalDate dateIssued;
}

