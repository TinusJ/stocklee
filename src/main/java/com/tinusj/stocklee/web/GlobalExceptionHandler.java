package com.tinusj.stocklee.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;

/**
 * Global exception handler for web controllers.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, Model model) {
        log.error("Runtime exception occurred: ", ex);
        model.addAttribute("errorTitle", "Error");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/general";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationException(MethodArgumentNotValidException ex, Model model) {
        log.error("Validation exception occurred: ", ex);
        
        StringBuilder errorMessage = new StringBuilder("Validation failed: ");
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errorMessage.append(error.getField())
                      .append(" - ")
                      .append(error.getDefaultMessage())
                      .append("; ");
        }
        
        model.addAttribute("errorTitle", "Validation Error");
        model.addAttribute("errorMessage", errorMessage.toString());
        return "error/general";
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public String handleConstraintViolationException(ConstraintViolationException ex, Model model) {
        log.error("Constraint violation exception occurred: ", ex);
        model.addAttribute("errorTitle", "Validation Error");
        model.addAttribute("errorMessage", "Validation failed: " + ex.getMessage());
        return "error/general";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        log.error("Unexpected exception occurred: ", ex);
        model.addAttribute("errorTitle", "Unexpected Error");
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again.");
        return "error/general";
    }
}