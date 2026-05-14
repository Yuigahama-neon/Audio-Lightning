package com.example.audioplatform.controller;

import com.example.audioplatform.service.AdminService;
import com.example.audioplatform.service.AudioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final AudioService audioService;

    public AdminController(AdminService adminService, AudioService audioService) {
        this.adminService = adminService;
        this.audioService = audioService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("users", adminService.findAllUsers());
        model.addAttribute("tracks", audioService.findAllForAdmin());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", adminService.findAllUsers());
        return "admin/users";
    }

    @PostMapping("/users/{id}/block")
    public String toggleBlock(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminService.toggleBlock(id);
        redirectAttributes.addFlashAttribute("success", "Статус пользователя изменён.");
        return "redirect:/admin/users";
    }

    @GetMapping("/audio")
    public String audio(Model model) {
        model.addAttribute("tracks", audioService.findAllForAdmin());
        return "admin/audio";
    }

    @PostMapping("/audio/{id}/delete")
    public String deleteAudio(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        audioService.deleteAsAdmin(id);
        redirectAttributes.addFlashAttribute("success", "Аудиозапись удалена.");
        return "redirect:/admin/audio";
    }
}
