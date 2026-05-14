package com.example.audioplatform.service;

import com.example.audioplatform.dto.AudioUpdateDto;
import com.example.audioplatform.dto.AudioUploadDto;
import com.example.audioplatform.entity.AudioTrack;
import com.example.audioplatform.entity.Genre;
import com.example.audioplatform.entity.User;
import com.example.audioplatform.repository.AudioTrackRepository;
import com.example.audioplatform.repository.GenreRepository;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AudioService {

    private final AudioTrackRepository audioTrackRepository;
    private final GenreRepository genreRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    public AudioService(AudioTrackRepository audioTrackRepository,
                        GenreRepository genreRepository,
                        UserService userService,
                        FileStorageService fileStorageService) {
        this.audioTrackRepository = audioTrackRepository;
        this.genreRepository = genreRepository;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public List<AudioTrack> findForUser(String username, String query, Long genreId) {
        User owner = userService.findByUsernameOrEmail(username);
        return audioTrackRepository.searchOwned(owner, normalizeQuery(query), genreId);
    }

    @Transactional(readOnly = true)
    public List<AudioTrack> findAllForAdmin() {
        return audioTrackRepository.findAllByOrderByUploadedAtDesc();
    }

    @Transactional(readOnly = true)
    public long countForUser(String username) {
        return audioTrackRepository.countByOwner(userService.findByUsernameOrEmail(username));
    }

    @Transactional
    public AudioTrack upload(AudioUploadDto dto, String username) {
        User owner = userService.findByUsernameOrEmail(username);
        Genre genre = genreRepository.findById(dto.getGenreId())
                .orElseThrow(() -> new IllegalArgumentException("Жанр не найден"));

        FileStorageService.StoredFile storedFile = fileStorageService.store(dto.getFile(), owner.getId());

        AudioTrack track = new AudioTrack();
        track.setTitle(dto.getTitle().trim());
        track.setArtist(blankToNull(dto.getArtist()));
        track.setDescription(blankToNull(dto.getDescription()));
        track.setGenre(genre);
        track.setFileName(storedFile.fileName());
        track.setFilePath(storedFile.filePath());
        track.setFileSize(storedFile.fileSize());
        track.setMimeType(storedFile.mimeType());
        track.setUploadedAt(LocalDateTime.now());
        track.setPlayCount(0);
        track.setOwner(owner);
        return audioTrackRepository.save(track);
    }

    @Transactional(readOnly = true)
    public AudioTrack getAccessibleTrack(Long id, String username) {
        User user = userService.findByUsernameOrEmail(username);
        AudioTrack track = audioTrackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Аудиозапись не найдена"));
        assertCanAccess(user, track);
        return track;
    }

    @Transactional
    public AudioTrack update(Long id, AudioUpdateDto dto, String username) {
        User user = userService.findByUsernameOrEmail(username);
        AudioTrack track = audioTrackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Аудиозапись не найдена"));
        assertCanAccess(user, track);

        Genre genre = genreRepository.findById(dto.getGenreId())
                .orElseThrow(() -> new IllegalArgumentException("Жанр не найден"));

        track.setTitle(dto.getTitle().trim());
        track.setArtist(blankToNull(dto.getArtist()));
        track.setDescription(blankToNull(dto.getDescription()));
        track.setGenre(genre);
        return audioTrackRepository.save(track);
    }

    @Transactional
    public void delete(Long id, String username) {
        User user = userService.findByUsernameOrEmail(username);
        deleteAsUser(id, user);
    }

    @Transactional
    public void deleteAsAdmin(Long id) {
        AudioTrack track = audioTrackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Аудиозапись не найдена"));
        deleteTrack(track);
    }

    @Transactional
    public Resource loadForStreaming(Long id, String username) {
        User user = userService.findByUsernameOrEmail(username);
        AudioTrack track = audioTrackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Аудиозапись не найдена"));
        assertCanAccess(user, track);
        track.setPlayCount(track.getPlayCount() + 1);
        audioTrackRepository.save(track);
        return fileStorageService.loadAsResource(track.getFilePath());
    }

    @Transactional(readOnly = true)
    public String getMimeType(Long id, String username) {
        return getAccessibleTrack(id, username).getMimeType();
    }

    private void deleteAsUser(Long id, User user) {
        AudioTrack track = audioTrackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Аудиозапись не найдена"));
        assertCanAccess(user, track);
        deleteTrack(track);
    }

    private void deleteTrack(AudioTrack track) {
        audioTrackRepository.delete(track);
        fileStorageService.delete(track.getFilePath());
    }

    private void assertCanAccess(User user, AudioTrack track) {
        if (!user.isAdmin() && !track.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Нет доступа к этой аудиозаписи");
        }
    }

    private String normalizeQuery(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
