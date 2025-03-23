package com.optimednotifications.repository;

import com.optimednotifications.entity.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface RecipientRepository extends JpaRepository<Recipient, UUID> {
    Optional<Recipient> findByEmail(String email);
}
