package com.optimed.entity;

import com.optimed.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.*;
import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    private String fullName;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 500, nullable = true)
    private String address;

    @Column(unique = true)
    private String phoneNumber;

    @Column(unique = true)
    private String email;

    @Column(length = 2000, nullable = true)
    private String medicalHistory;

    @Column(nullable = true)
    private String avatarUrl;

    @ManyToMany(mappedBy = "patients")
    private List<DoctorProfile> doctors;
}

