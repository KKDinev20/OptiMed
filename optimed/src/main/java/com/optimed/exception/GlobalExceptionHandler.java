package com.optimed.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.*;
import org.springframework.web.servlet.view.*;

import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DoctorNotFoundException.class)
    public RedirectView handleDoctorNotFound(DoctorNotFoundException ex, RedirectAttributes attributes) {
        logger.error("Doctor Not Found Exception: ", ex);
        attributes.addFlashAttribute("errorMessage", ex.getMessage());
        return new RedirectView("/login?error=true");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public String handleNotFoundException(NoSuchElementException e, Model model) {
        logger.error("Resource Not Found Exception: ", e);
        model.addAttribute("error", "Resource not found.");
        model.addAttribute("errorDetails", e.getMessage());
        return "error/error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e, Model model) {
        logger.error("Unexpected error occurred: ", e);

        model.addAttribute("error", "Something went wrong. Please try again.");
        model.addAttribute("errorDetails", e.getMessage());
        model.addAttribute("errorType", e.getClass().getSimpleName());

        return "error/error";
    }
}