package com.example.audioplatform.controller;

import com.example.audioplatform.entity.User;
import com.example.audioplatform.service.AudioService;
import com.example.audioplatform.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class ProfileController {

    private final UserService userService;
    private final AudioService audioService;

    public ProfileController(UserService userService, AudioService audioService) {
        this.userService = userService;
        this.audioService = audioService;
    }

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        User user = userService.findByUsernameOrEmail(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("audioCount", audioService.countForUser(principal.getName()));
        return "profile/profile";
    }
}
