package com.optimed.repository;

import com.optimed.entity.PatientProfile;
import com.optimed.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface PatientRepository extends JpaRepository<PatientProfile, UUID> {
    Optional<PatientProfile> findByUserUsername(String username);
    Optional<PatientProfile> findByUser(User user);
    Optional<PatientProfile> findByUserId(UUID userId);
}
