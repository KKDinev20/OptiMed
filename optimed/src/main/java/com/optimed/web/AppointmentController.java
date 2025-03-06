package com.optimed.web;

import com.optimed.dto.AppointmentRequest;
import com.optimed.entity.Appointment;
import com.optimed.entity.PatientProfile;
import com.optimed.service.AppointmentService;
import com.optimed.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final PatientService patientService;

    @GetMapping
    public String listAppointments(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String filter,
                                   Model model) {

        Page<Appointment> appointments = appointmentService.getAllAppointments(filter, PageRequest.of(page, size));

        model.addAttribute("appointments", appointments);
        model.addAttribute("currentPage", "Manage Appointments");
        model.addAttribute("pageSize", size);
        model.addAttribute("pageSizes", Arrays.asList(5, 10, 15, 20));

        return "doctor/appointments";
    }

    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @GetMapping("/book")
    public String showBookingForm(Model model) {
        model.addAttribute("appointmentRequest", new AppointmentRequest ());
        return "patient/book-appointment";
    }

    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @PostMapping("/book")
    public String bookAppointment(@ModelAttribute @Valid AppointmentRequest request, Principal principal, Model model) {
        Optional<PatientProfile> patient = patientService.getPatientByUsername(principal.getName());

        if (patient.isEmpty()) {
            model.addAttribute("error", "Patient profile not found.");
            return "patient/book-appointment";
        }

        request.setPatientId(patient.get().getId());
        appointmentService.createAppointment(request);

        return "redirect:/patient/appointments/success";
    }


    @PreAuthorize("hasAuthority('ROLE_PATIENT')")
    @GetMapping("/success")
    public String showSuccessPage() {
        return "patient/appointment-success";
    }

}
