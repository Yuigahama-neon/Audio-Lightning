package com.example.audioplatform.controller;

import com.example.audioplatform.service.GenreService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class GenreController {

    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @PostMapping("/genres")
    public String create(@RequestParam String name,
                         @RequestParam(defaultValue = "/audio/upload") String returnTo,
                         RedirectAttributes redirectAttributes) {
        try {
            genreService.create(name);
            redirectAttributes.addFlashAttribute("success", "Жанр добавлен.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:" + safeReturnTo(returnTo);
    }

    private String safeReturnTo(String returnTo) {
        if (returnTo == null || returnTo.isBlank() || !returnTo.startsWith("/") || returnTo.startsWith("//")) {
            return "/audio/upload";
        }
        return returnTo;
    }
}
