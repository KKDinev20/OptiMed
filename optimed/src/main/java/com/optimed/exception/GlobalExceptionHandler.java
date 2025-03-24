/*
package com.optimed.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.*;
import org.springframework.web.servlet.view.*;

import java.util.NoSuchElementException;

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

    @ExceptionHandler(NoSuchElementException.class)
    public String handleNotFoundException(NoSuchElementException e, Model model) {
        model.addAttribute("error", "Resource not found.");
        return "error/404"; // Custom error page
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e, Model model) {
        model.addAttribute("error", "Something went wrong. Please try again.");
        return "error/500"; // Generic error page
    }
}*/
