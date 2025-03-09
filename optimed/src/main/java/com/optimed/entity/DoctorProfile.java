package com.optimed.entity;

import com.optimed.entity.enums.Gender;
import com.optimed.entity.enums.Specialization;
import jakarta.persistence.*;
import lombok.*;

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
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    private String fullName;

    @Enumerated(EnumType.STRING)
    private Specialization specialization;

    private int experienceYears;

    private String email;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 2000)
    private String bio;

    @Column(length = 500)
    private String availabilitySchedule;

    private String contactInfo;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<Appointment> appointments;

    private String avatarUrl;
}
