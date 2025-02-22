package com.optimed.service;

import com.optimed.dto.RegisterRequest;
import com.optimed.entity.*;
import com.optimed.entity.enums.Role;
import com.optimed.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

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
}
