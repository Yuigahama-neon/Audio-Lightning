package com.example.audioplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AudioUpdateDto {

    @NotBlank(message = "Введите название аудиозаписи")
    @Size(max = 180, message = "Название слишком длинное")
    private String title;

    @Size(max = 160, message = "Имя автора слишком длинное")
    private String artist;

    @NotNull(message = "Выберите жанр")
    private Long genreId;

    @Size(max = 2000, message = "Описание слишком длинное")
    private String description;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Long getGenreId() {
        return genreId;
    }

    public void setGenreId(Long genreId) {
        this.genreId = genreId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
