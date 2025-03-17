package com.optimed.repository;

import com.optimed.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    List<User> findTop10ByOrderByIdDesc();

    @Query("SELECT u FROM User u WHERE u.role <> 'ADMIN'")
    Page<User> findAllNonAdminUsers(Pageable pageable);
}

