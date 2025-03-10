package com.optimed.web;

import com.optimed.dto.AppointmentRequest;
import com.optimed.entity.*;
import com.optimed.entity.enums.Specialization;
import com.optimed.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/patient")
@PreAuthorize("hasRole('ROLE_PATIENT')")
@RequiredArgsConstructor
public class PatientController {

    private final AppointmentService appointmentService;
    private final DoctorProfileService doctorProfileService;
    private final UserService userService;
    private final PatientService patientService;

    @GetMapping("/appointments/new")
    public String showAppointmentForm(Model model) {
        model.addAttribute("appointmentRequest", new AppointmentRequest());
        model.addAttribute("currentUserPage", "Add Appointment");
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
        return "redirect:/patient/appointments";
    }


    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName()).orElseThrow();
        PatientProfile patient = patientService.findByUser(user).orElseThrow();

        UUID patientId = patient.getId();
        model.addAttribute("currentUserPage", "Dashboard");
        model.addAttribute("patientId", patientId);

        return "patient/dashboard";
    }

    @GetMapping("/doctors/{specialization}")
    @ResponseBody
    public List<DoctorProfile> getDoctorsBySpecialization(@PathVariable Specialization specialization) {
        return doctorProfileService.findDoctorsBySpecialization(specialization);
    }

    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @GetMapping("/appointments")
    public String getPatientAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            Principal principal) {

        User user = userService.findByUsername(principal.getName()).orElseThrow();
        PatientProfile patient = patientService.findByUser(user).orElseThrow();
        UUID patientId = patient.getId();

        Pageable pageable = PageRequest.of(page, size);
        Page<Appointment> appointments = appointmentService.getAppointmentsByPatientId(patientId, pageable);

        model.addAttribute("appointments", appointments);
        model.addAttribute("patientId", patientId);
        model.addAttribute("currentPage", page);
        model.addAttribute("currentUserPage", "My Appointments");
        model.addAttribute("totalPages", appointments.getTotalPages());
        model.addAttribute("size", size);

        return "patient/appointments/list-appointments";
    }

    @PostMapping("/cancel-appointment/{appointmentId}")
    public String cancelAppointment(
            @PathVariable UUID appointmentId,
            RedirectAttributes redirectAttrs
    ) {
        appointmentService.cancelAppointment(appointmentId);
        return "redirect:/patient/appointments";
    }
}
