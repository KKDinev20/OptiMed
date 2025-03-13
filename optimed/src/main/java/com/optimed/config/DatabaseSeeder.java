package com.optimed.config;

import com.optimed.entity.User;
import com.optimed.entity.enums.Role;
import com.optimed.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdminUser();
    }

    private void seedAdminUser() {
        String adminUsername = "admin";
        Optional<User> existingAdmin = userRepository.findByUsername(adminUsername);

        if (existingAdmin.isEmpty()) {
            User admin = User.builder()
                    .username(adminUsername)
                    .email("admin@optimed.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .enabled (true)
                    .build();

            userRepository.save(admin);
            System.out.println("Admin user created: " + adminUsername);
        } else {
            System.out.println("Admin user already exists.");
        }
    }
}
