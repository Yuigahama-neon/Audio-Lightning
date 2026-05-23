package com.example.audioplatform.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("mp3", "wav", "ogg", "m4a");
    private final Path audioRoot;
    private final long maxFileSizeBytes;

    public FileStorageService(@Value("${app.storage.audio-path}") String audioPath,
                              @Value("${app.storage.max-file-size-bytes}") long maxFileSizeBytes) {
        this.audioRoot = Paths.get(audioPath).toAbsolutePath().normalize();
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(audioRoot);
    }

    public StoredFile store(MultipartFile file, Long userId) {
        validate(file);

        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (originalFileName.contains("..")) {
            throw new IllegalArgumentException("Недопустимое имя файла");
        }

        String safeOriginalName = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String storedFileName = userId + "_" + System.currentTimeMillis() + "_" + safeOriginalName;
        Path destination = audioRoot.resolve(storedFileName).normalize();

        if (!destination.startsWith(audioRoot)) {
            throw new IllegalArgumentException("Недопустимый путь файла");
        }

        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Не удалось сохранить файл", ex);
        }

        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        return new StoredFile(storedFileName, destination.toString(), file.getSize(), contentType);
    }

    public Resource loadAsResource(String filePath) {
        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            if (!path.startsWith(audioRoot)) {
                throw new IllegalArgumentException("Недопустимый путь файла");
            }
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("Аудиофайл не найден");
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Некорректный путь файла", ex);
        }
    }

    public void delete(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }
        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            if (path.startsWith(audioRoot)) {
                Files.deleteIfExists(path);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Не удалось удалить файл", ex);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Выберите непустой аудиофайл");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new IllegalArgumentException("Размер файла превышает лимит 50 МБ");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("Имя файла не определено");
        }

        String extension = StringUtils.getFilenameExtension(originalFileName);
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Допустимые форматы: .mp3, .wav, .ogg, .m4a");
        }
    }

    public record StoredFile(String fileName, String filePath, long fileSize, String mimeType) {
    }
}
