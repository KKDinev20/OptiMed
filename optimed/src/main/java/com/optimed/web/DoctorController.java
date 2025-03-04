package com.optimed.web;

import com.optimed.dto.DoctorRequest;
import com.optimed.entity.DoctorProfile;
import com.optimed.service.DoctorProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorProfileService doctorProfileService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof UserDetails userDetails) {
            model.addAttribute("username", userDetails.getUsername());
        }
        return "doctor/dashboard";
    }

    @GetMapping("/{doctorId}")
    public String getDoctorProfile(@PathVariable UUID doctorId, Model model) {
        doctorProfileService.getDoctorByUserId(doctorId).ifPresent(doctor -> model.addAttribute("doctor", doctor));
        return "doctor/profile";
    }

    @PutMapping("/{doctorId}")
    @ResponseBody
    public DoctorProfile updateDoctorProfile(@PathVariable UUID doctorId, @Valid @RequestBody DoctorRequest doctorProfileRequest) {
        return doctorProfileService.updateDoctorProfile(doctorId, doctorProfileRequest);
    }
}
