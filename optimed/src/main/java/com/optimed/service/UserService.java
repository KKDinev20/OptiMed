package com.optimed.service;

import com.optimed.dto.*;
import com.optimed.entity.*;
import com.optimed.entity.enums.Role;
import com.optimed.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findByUsername (String username) {
        return userRepository.findByUsername (username);
    }

    public void registerUser(RegisterRequest registrationDto) {
        Role selectedRole = registrationDto.getRole () != null ? registrationDto.getRole () : Role.PATIENT;

        User user = User.builder ()
                .username (registrationDto.getUsername ())
                .password (passwordEncoder.encode (registrationDto.getPassword ()))
                .email (registrationDto.getEmail ())
                .enabled (false)
                .role (selectedRole)
                .build ();
        userRepository.save (user);

        if (selectedRole == Role.DOCTOR) {
            DoctorProfile doctorProfile = DoctorProfile.builder ()
                    .user (user)
                    .build ();
            doctorRepository.save (doctorProfile);
        } else {
            PatientProfile patientProfile = PatientProfile.builder ()
                    .user (user)
                    .build ();
            patientRepository.save (patientProfile);
        }
    }
    public long countUsers () {
        return userRepository.count ();
    }


    public List<User> getRecentUsers () {
        return userRepository.findTop10ByOrderByIdDesc ();
    }

    public User getUserById (UUID userId) {
        return userRepository.findById (userId)
                .orElseThrow (() -> new EntityNotFoundException ("User not found with ID: " + userId));
    }

    public Page<User> getAllNonAdminUsers (Pageable pageable) {
        return userRepository.findAllNonAdminUsers (pageable);
    }

    public void updateUser (UUID userId, UserRequest userRequest) {
        User existingUser = userRepository.findById (userId)
                .orElseThrow (() -> new EntityNotFoundException ("User not found with ID: " + userId));

        existingUser.setUsername (userRequest.getUsername ());
        existingUser.setEmail (userRequest.getEmail ());
        existingUser.setRole (userRequest.getRole ());

        userRepository.save (existingUser);
    }


    public void completeDoctorProfile(String username, DoctorRequest request) {
        User user = findByUsername(username).orElseThrow();
        DoctorProfile profile = doctorRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    DoctorProfile newProfile = new DoctorProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        profile.setFullName(request.getFullName());
        profile.setEmail(user.getEmail());
        profile.setAvatarUrl(request.getAvatarUrl());
        profile.setSpecialization(request.getSpecialization());
        profile.setGender(request.getGender());
        profile.setExperienceYears(request.getExperienceYears());
        profile.setBio(request.getBio());
        profile.setAvailableDays(request.getAvailableDays());
        profile.setContactInfo(request.getContactInfo());

        profile.setAvailableTimeSlots(request.getAvailableTimeSlots());

        user.setEnabled(true);

        doctorRepository.save(profile);
        userRepository.save(user);
    }



    public void completePatientProfile(String username, PatientRequest request) {
        User user = findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        PatientProfile profile = patientRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    PatientProfile newProfile = new PatientProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        profile.setFullName(request.getFullName());
        profile.setAvatarUrl(request.getAvatarUrl());
        profile.setEmail (user.getEmail ());
        profile.setGender (request.getGender());
        profile.setAddress(request.getAddress());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setDateOfBirth (request.getDateOfBirth());
        profile.setMedicalHistory(request.getMedicalHistory());

        user.setEnabled (true);

        patientRepository.save(profile);
        userRepository.save(user);
    }

    public void deleteUser(UUID userId) {
        userRepository.deleteById(userId);
    }

    public String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "/dashboard/img/default.png";
        }

        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Only image files are allowed.");
            }

            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
            Path uploadDir = Paths.get("D:/OptiMed/optimed/src/main/resources/static/dashboard/img");

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path filePath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/dashboard/img/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image file", e);
        }
    }

}
