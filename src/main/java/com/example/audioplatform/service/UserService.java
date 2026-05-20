package com.example.audioplatform.service;

import com.example.audioplatform.dto.UserRegisterDto;
import com.example.audioplatform.entity.Role;
import com.example.audioplatform.entity.User;
import com.example.audioplatform.repository.RoleRepository;
import com.example.audioplatform.repository.UserRepository;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    @Transactional
    public User register(UserRegisterDto dto) {
        if (!dto.getPassword().equals(dto.getRepeatPassword())) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }
        String username = dto.getUsername().trim();
        String email = dto.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Роль ROLE_USER не создана"));

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        user.setEnabled(true);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    @Transactional
    public User updateAvatar(String usernameOrEmail, MultipartFile avatar) {
        User user = findByUsernameOrEmail(usernameOrEmail);
        String previousAvatarPath = user.getAvatarFilePath();
        FileStorageService.StoredFile storedFile = fileStorageService.storeAvatar(avatar, user.getId());

        user.setAvatarFileName(storedFile.fileName());
        user.setAvatarFilePath(storedFile.filePath());
        user.setAvatarMimeType(storedFile.mimeType());
        User savedUser = userRepository.save(user);

        if (previousAvatarPath != null && !previousAvatarPath.equals(storedFile.filePath())) {
            fileStorageService.deleteAvatar(previousAvatarPath);
        }

        return savedUser;
    }

    @Transactional(readOnly = true)
    public Resource loadAvatar(String usernameOrEmail) {
        User user = findByUsernameOrEmail(usernameOrEmail);
        if (!user.hasAvatar()) {
            throw new IllegalArgumentException("Аватарка не загружена");
        }
        return fileStorageService.loadAvatarAsResource(user.getAvatarFilePath());
    }
}
