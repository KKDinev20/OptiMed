package com.optimed.web;

import lombok.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/patient")
@PreAuthorize("hasRole('ROLE_PATIENT')")
@RequiredArgsConstructor
public class PatientController {

    @GetMapping("/dashboard")
    public ModelAndView dashboard(Model model) {
        return new ModelAndView("patient/dashboard");
    }

}
