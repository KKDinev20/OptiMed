package com.optimed.web;

import com.optimed.dto.AppointmentRequest;
import com.optimed.entity.DoctorProfile;
import com.optimed.entity.PatientProfile;
import com.optimed.entity.enums.Specialization;
import com.optimed.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/patient")
@PreAuthorize("hasRole('ROLE_PATIENT')")
@RequiredArgsConstructor
public class PatientController {

    private final AppointmentService appointmentService;
    private final DoctorProfileService doctorProfileService;
    private final PatientService patientService;

    @GetMapping("/appointments/new")
    public String showAppointmentForm(Model model) {
        model.addAttribute("appointmentRequest", new AppointmentRequest());
        model.addAttribute("specializations", Specialization.values());
        return "patient/appointments/create";
    }

    @PostMapping("/appointments")
    public String addAppointment(@ModelAttribute AppointmentRequest request, Authentication authentication, Model model) {
        Optional<PatientProfile> patient = patientService.getPatientByUsername(authentication.getName());

        if (patient.isEmpty()) {
            model.addAttribute("error", "Patient not found.");
            return "patient/appointments/create";
        }

        request.setPatientId(patient.get().getId());
        appointmentService.createAppointment(request);

        model.addAttribute("success", "Appointment created successfully.");
        return "redirect:/patient/dashboard";
    }


    @GetMapping("/doctors/{specialization}")
    @ResponseBody
    public List<DoctorProfile> getDoctorsBySpecialization(@PathVariable Specialization specialization) {
        return doctorProfileService.findDoctorsBySpecialization(specialization);
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        return "patient/dashboard";
    }
}
