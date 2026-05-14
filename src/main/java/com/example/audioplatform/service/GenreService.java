package com.example.audioplatform.service;

import com.example.audioplatform.entity.Genre;
import com.example.audioplatform.repository.GenreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GenreService {

    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Transactional(readOnly = true)
    public List<Genre> findAll() {
        return genreRepository.findAll().stream()
                .sorted((left, right) -> left.getName().compareToIgnoreCase(right.getName()))
                .toList();
    }

    @Transactional
    public Genre create(String name) {
        String normalizedName = normalize(name);
        return genreRepository.findByNameIgnoreCase(normalizedName)
                .orElseGet(() -> genreRepository.save(new Genre(normalizedName)));
    }

    private String normalize(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Введите название жанра");
        }

        String normalizedName = name.trim().replaceAll("\\s+", " ");
        if (normalizedName.length() > 80) {
            throw new IllegalArgumentException("Название жанра слишком длинное");
        }
        return normalizedName;
    }
}
