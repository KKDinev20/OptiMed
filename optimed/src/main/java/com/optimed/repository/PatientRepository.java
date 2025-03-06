package com.optimed.repository;

import com.optimed.entity.PatientProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface PatientRepository extends JpaRepository<PatientProfile, UUID> {
    Optional<PatientProfile> findByUserUsername(String username);
}
