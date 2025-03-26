package com.optimed.entity;

import com.optimed.entity.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    @Column
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column
    private Specialization specialization;

    @Column
    private int experienceYears;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column
    private Gender gender;

    @Column(length = 2000, nullable = true)
    private String bio;

    @ElementCollection
    @CollectionTable(name = "doctor_available_days", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "available_day")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> availableDays;

    @ElementCollection
    @CollectionTable(name = "doctor_available_slots", joinColumns = @JoinColumn(name = "doctor_id"))
    private List<TimeSlot> availableTimeSlots;


    @Column
    private String contactInfo;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<Appointment> appointments;

    @ManyToMany
    @JoinTable(
            name = "patient_doctor_appointments",
            joinColumns = @JoinColumn(name = "doctor_id"),
            inverseJoinColumns = @JoinColumn(name = "patient_id"))
    private List<PatientProfile> patients;

    @Column(nullable = true)
    private String avatarUrl;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<Review> reviews;
}
