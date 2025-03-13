package com.optimed.web;

import com.optimed.entity.*;
import com.optimed.entity.enums.AppointmentStatus;
import com.optimed.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@Controller
@RequestMapping("/doctor")
@PreAuthorize("hasRole('ROLE_DOCTOR')")
@RequiredArgsConstructor
public class DoctorController {

    private final AppointmentService appointmentService;
    private final DoctorService doctorService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @PageableDefault(size = 10, sort = "appointmentDate", direction = Sort.Direction.ASC) Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof UserDetails userDetails) {
            String doctor = doctorService.getDoctorFullName(userDetails.getUsername());
            model.addAttribute("doctor", doctor);
        }

        model.addAttribute("cancelledAppointments", appointmentService.getCanceledAppointments());
        model.addAttribute("todaysAppointments", appointmentService.getTodaysAppointments());
        model.addAttribute("bookedAppointments", appointmentService.getBookedAppointments());

        Page<Appointment> upcomingAppointments = appointmentService.getUpcomingAppointmentsForMonth(pageable);
        model.addAttribute("upcomingAppointments", upcomingAppointments.getContent());

        return "doctor/dashboard";
    }


    @GetMapping("/appointments")
    public String getDoctorAppointments(
            Model model,
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        String username = authentication.getName();
        DoctorProfile doctor = doctorService.findByUsername(username).orElseThrow();

        Pageable pageable = PageRequest.of(page, size);
        Page<Appointment> appointmentPage = appointmentService.searchAppointments(doctor.getId(), status, patientName, startDate, endDate, pageable);

        model.addAttribute("appointments", appointmentPage.getContent());
        model.addAttribute("totalPages", appointmentPage.getTotalPages());
        model.addAttribute("totalItems", appointmentPage.getTotalElements());
        model.addAttribute("currentPage", appointmentPage.getNumber());
        model.addAttribute("pageSize", appointmentPage.getSize());

        return "doctor/appointments/list-appointments";
    }


    @PostMapping("/appointments/{appointmentId}/approve")
    public String approveAppointment(@PathVariable UUID appointmentId) {
        appointmentService.updateAppointmentStatus(appointmentId, AppointmentStatus.CONFIRMED);
        return "redirect:/doctor/appointments";
    }

    @PostMapping("/appointments/{appointmentId}/reject")
    public String rejectAppointment(@PathVariable UUID appointmentId) {
        appointmentService.updateAppointmentStatus(appointmentId, AppointmentStatus.CANCELED);
        return "redirect:/doctor/appointments";
    }

    @PostMapping("/appointments/{appointmentId}/cancel")
    public String cancelAppointment(@PathVariable UUID appointmentId) {
        appointmentService.cancelAppointment(appointmentId);
        return "redirect:/doctor/appointments";
    }

    @PostMapping("/appointments/{appointmentId}/reschedule")
    public String rescheduleAppointment(@PathVariable UUID appointmentId,
                                        @RequestParam("newDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newDate,
                                        @RequestParam("newTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime newTime) {
        appointmentService.rescheduleAppointment(appointmentId, newDate, newTime);
        return "redirect:/doctor/appointments";
    }

    @GetMapping("/appointments/{appointmentId}/patient")
    public String viewPatientDetails(@PathVariable UUID appointmentId, Model model) {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        model.addAttribute("patient", appointment.getPatient());
        return "doctor/patients/view-patient";
    }



    @GetMapping("/patients")
    public String getDoctorPatients(Model model, Authentication authentication) {
        String username = authentication.getName();
        DoctorProfile doctor = doctorService.findByUsername(username).orElseThrow();

        List<PatientProfile> patients = appointmentService.getPatientsByDoctor(doctor.getId());
        model.addAttribute("patients", patients);

        return "doctor/patients/list-patients";
    }
}
