package com.optimed.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/patient")
public class PatientController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('PATIENT')")
    public ModelAndView dashboard() {
        ModelAndView mav = new ModelAndView("patient/dashboard");
        return mav;
    }
}
