package com.optimed.web;

import com.optimed.entity.Appointment;
import com.optimed.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/doctor/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

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

    @PostMapping("/approve/{id}")
    public String approveAppointment(@PathVariable UUID id) {
        appointmentService.approveAppointment(id);
        return "redirect:/doctor/appointments";
    }

    @PostMapping("/cancel/{id}")
    public String cancelAppointment(@PathVariable UUID id) {
        appointmentService.cancelAppointment(id);
        return "redirect:/doctor/appointments";
    }
}
