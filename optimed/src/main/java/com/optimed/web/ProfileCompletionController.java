package com.optimed.web;

import com.optimed.dto.*;
import com.optimed.entity.User;
import com.optimed.entity.enums.Role;
import com.optimed.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Controller
@RequestMapping("/complete-profile")
@RequiredArgsConstructor
public class ProfileCompletionController {
    private final UserService userService;

    @GetMapping
    public String showProfileForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElseThrow();

        if (user.getRole().equals(Role.DOCTOR)) {
            model.addAttribute("doctorRequest", new DoctorRequest());
            return "doctor/complete-profile";
        } else {
            model.addAttribute("patientRequest", new PatientRequest());
            return "patient/complete-profile";
        }
    }

    @PostMapping("/doctor")
    public String completeDoctorProfile(
            @ModelAttribute DoctorRequest doctorRequest,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String imageUrl = storeImage(avatarFile);
            doctorRequest.setAvatarUrl(imageUrl);
        } else {
            doctorRequest.setAvatarUrl("/dashboard/img/default.png");
        }

        userService.completeDoctorProfile (userDetails.getUsername(), doctorRequest);
        return "redirect:/doctor/dashboard";
    }


    @PostMapping("/patient")
    public String completePatientProfile(
            @ModelAttribute PatientRequest patientRequest,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String imageUrl = storeImage(avatarFile);
            patientRequest.setAvatarUrl(imageUrl);
        } else {
            patientRequest.setAvatarUrl("/dashboard/img/default.png");
        }

        userService.completePatientProfile(userDetails.getUsername(), patientRequest);
        return "redirect:/patient/dashboard";
    }

    private String storeImage(MultipartFile file) {
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
