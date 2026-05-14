package com.example.audioplatform.controller;

import com.example.audioplatform.entity.User;
import com.example.audioplatform.service.AudioService;
import com.example.audioplatform.service.UserService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @PostMapping("/profile/avatar")
    public String uploadAvatar(@RequestParam("avatar") MultipartFile avatar,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            userService.updateAvatar(principal.getName(), avatar);
            redirectAttributes.addFlashAttribute("success", "Аватарка обновлена.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/profile";
    }

    @GetMapping("/profile/avatar")
    public ResponseEntity<Resource> avatar(Principal principal) {
        User user = userService.findByUsernameOrEmail(principal.getName());
        Resource avatar = userService.loadAvatar(principal.getName());
        String mimeType = user.getAvatarMimeType() == null ? "application/octet-stream" : user.getAvatarMimeType();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .body(avatar);
    }
}
