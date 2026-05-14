package com.example.audioplatform.repository;

import com.example.audioplatform.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    Optional<Genre> findByName(String name);

    Optional<Genre> findByNameIgnoreCase(String name);
}
