package com.example.audioplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class AudioUploadDto {

    @NotBlank(message = "Введите название аудиозаписи")
    @Size(max = 180, message = "Название слишком длинное")
    private String title;

    @Size(max = 160, message = "Имя автора слишком длинное")
    private String artist;

    @Size(max = 2000, message = "Описание слишком длинное")
    private String description;

    @NotNull(message = "Выберите аудиофайл")
    private MultipartFile file;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
