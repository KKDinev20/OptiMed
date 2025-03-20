package com.optimed.exception;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.*;
import org.springframework.web.servlet.view.*;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DoctorNotFoundException.class)
    public RedirectView handleDoctorNotFound(DoctorNotFoundException ex, RedirectAttributes attributes) {
        attributes.addFlashAttribute("errorMessage", ex.getMessage());
        return new RedirectView("/login?error=true");
    }

    @ExceptionHandler(Exception.class)
    public RedirectView handleGeneralException(Exception ex, RedirectAttributes attributes) {
        attributes.addFlashAttribute("errorMessage",
                "Something went wrong. Please try again later.");
        return new RedirectView("/error");
    }
}