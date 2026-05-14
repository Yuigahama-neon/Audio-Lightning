package com.example.audioplatform.controller;

import com.example.audioplatform.entity.User;
import com.example.audioplatform.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CurrentUserAdvice {

    private final UserService userService;

    public CurrentUserAdvice(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute
    public void addCurrentUser(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return;
        }

        User currentUser = userService.findByUsernameOrEmail(authentication.getName());
        model.addAttribute("currentUser", currentUser);
    }
}
