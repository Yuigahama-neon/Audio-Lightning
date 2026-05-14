package com.example.audioplatform.controller;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public String accessDenied(AccessDeniedException ex, Model model) {
        model.addAttribute("status", 403);
        model.addAttribute("message", ex.getMessage());
        return "error";
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public String badRequest(RuntimeException ex, Model model) {
        model.addAttribute("status", 400);
        model.addAttribute("message", ex.getMessage());
        return "error";
    }
}
