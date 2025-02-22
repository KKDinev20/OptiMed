package com.optimed.repository;

import com.optimed.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    List<User> findTop10ByOrderByIdDesc();
    Page<User> findAll(org.springframework.data.domain.Pageable pageable);
}

