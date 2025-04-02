package com.optimed.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationErrors(MethodArgumentNotValidException ex, Model model) {
        logger.warn("Validation error: {}", ex.getMessage());

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        model.addAttribute("error", "Validation failed");
        model.addAttribute("errorDetails", errors);
        return "error/error";
    }

    @ExceptionHandler(DoctorNotFoundException.class)
    public String handleDoctorNotFound(DoctorNotFoundException ex, Model model) {
        logger.error("Doctor Not Found Exception: {}", ex.getMessage());
        model.addAttribute("error", "Doctor not found.");
        model.addAttribute("errorDetails", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        logger.error("Unexpected error: ", ex);
        model.addAttribute("error", "Something went wrong. Please try again.");
        model.addAttribute("errorDetails", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(ConversionNotSupportedException.class)
    public String handleConversionErrors(ConversionNotSupportedException ex, Model model) {
        logger.error("Conversion error: {}", ex.getMessage());
        model.addAttribute("error", "Invalid data format");
        model.addAttribute("errorDetails", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFound(UserNotFoundException ex, Model model) {
        logger.error("User Not Found Exception: ", ex);
        model.addAttribute("error", "User not found.");
        model.addAttribute("errorDetails", ex.getMessage());
        return "error/error";
    }

}
