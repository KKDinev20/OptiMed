package com.optimed.service;

import com.optimed.dto.RegisterRequest;
import com.optimed.dto.UserRequest;
import com.optimed.entity.*;
import com.optimed.entity.enums.Role;
import com.optimed.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User registerUser(RegisterRequest registrationDto) {
        User user = User.builder()
                .username(registrationDto.getUsername())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .email(registrationDto.getEmail())
                .role(Role.PATIENT)
                .build();
        return userRepository.save(user);
    }

    public long countUsers () {
        return userRepository.count ();
    }

    public List<User> getRecentUsers () {
        return userRepository.findTop10ByOrderByIdDesc();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User updateUser(UUID userId, UserRequest userRequest) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException ("User not found with ID: " + userId));

        existingUser.setUsername(userRequest.getUsername());
        existingUser.setEmail(userRequest.getEmail());
        existingUser.setRole(userRequest.getRole());

        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }

        return userRepository.save(existingUser);
    }
}
