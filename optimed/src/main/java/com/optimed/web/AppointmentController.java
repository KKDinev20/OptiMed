package com.optimed.web;

import com.optimed.service.AppointmentService;
import com.optimed.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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


        model.addAttribute("currentPage", "Manage Appointments");
        model.addAttribute("pageSize", size);
        model.addAttribute("pageSizes", Arrays.asList(5, 10, 15, 20));

        return "doctor/appointments";
    }


}
