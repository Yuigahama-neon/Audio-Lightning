package com.example.audioplatform.controller;

import com.example.audioplatform.dto.AudioUpdateDto;
import com.example.audioplatform.dto.AudioUploadDto;
import com.example.audioplatform.entity.AudioTrack;
import com.example.audioplatform.repository.GenreRepository;
import com.example.audioplatform.service.AudioService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/audio")
public class AudioController {

    private final AudioService audioService;
    private final GenreRepository genreRepository;

    public AudioController(AudioService audioService, GenreRepository genreRepository) {
        this.audioService = audioService;
        this.genreRepository = genreRepository;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String query,
                       @RequestParam(required = false) Long genreId,
                       Principal principal,
                       Model model) {
        model.addAttribute("tracks", audioService.findForUser(principal.getName(), query, genreId));
        model.addAttribute("genres", genreRepository.findAll());
        model.addAttribute("query", query);
        model.addAttribute("genreId", genreId);
        return "audio/list";
    }

    @GetMapping("/upload")
    public String uploadForm(Model model) {
        if (!model.containsAttribute("audioUploadDto")) {
            model.addAttribute("audioUploadDto", new AudioUploadDto());
        }
        model.addAttribute("genres", genreRepository.findAll());
        return "audio/upload";
    }

    @PostMapping("/upload")
    public String upload(@Valid @ModelAttribute("audioUploadDto") AudioUploadDto dto,
                         BindingResult bindingResult,
                         Principal principal,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("genres", genreRepository.findAll());
            return "audio/upload";
        }

        try {
            audioService.upload(dto, principal.getName());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            bindingResult.reject("uploadError", ex.getMessage());
            model.addAttribute("genres", genreRepository.findAll());
            return "audio/upload";
        }

        redirectAttributes.addFlashAttribute("success", "Аудиозапись загружена.");
        return "redirect:/audio";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("track", audioService.getAccessibleTrack(id, principal.getName()));
        return "audio/details";
    }

    @GetMapping("/{id}/stream")
    public ResponseEntity<Resource> stream(@PathVariable Long id, Principal principal) {
        Resource resource = audioService.loadForStreaming(id, principal.getName());
        String mimeType = audioService.getMimeType(id, principal.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Principal principal, Model model) {
        AudioTrack track = audioService.getAccessibleTrack(id, principal.getName());
        AudioUpdateDto dto = new AudioUpdateDto();
        dto.setTitle(track.getTitle());
        dto.setArtist(track.getArtist());
        dto.setDescription(track.getDescription());
        dto.setGenreId(track.getGenre().getId());

        model.addAttribute("track", track);
        model.addAttribute("audioUpdateDto", dto);
        model.addAttribute("genres", genreRepository.findAll());
        return "audio/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("audioUpdateDto") AudioUpdateDto dto,
                       BindingResult bindingResult,
                       Principal principal,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        AudioTrack track = audioService.getAccessibleTrack(id, principal.getName());
        if (bindingResult.hasErrors()) {
            model.addAttribute("track", track);
            model.addAttribute("genres", genreRepository.findAll());
            return "audio/edit";
        }

        try {
            audioService.update(id, dto, principal.getName());
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("updateError", ex.getMessage());
            model.addAttribute("track", track);
            model.addAttribute("genres", genreRepository.findAll());
            return "audio/edit";
        }

        redirectAttributes.addFlashAttribute("success", "Аудиозапись обновлена.");
        return "redirect:/audio/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        audioService.delete(id, principal.getName());
        redirectAttributes.addFlashAttribute("success", "Аудиозапись удалена.");
        return "redirect:/audio";
    }
}
