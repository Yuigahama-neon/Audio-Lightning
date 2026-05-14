package com.example.audioplatform.config;

import com.example.audioplatform.entity.Genre;
import com.example.audioplatform.entity.Role;
import com.example.audioplatform.entity.User;
import com.example.audioplatform.repository.GenreRepository;
import com.example.audioplatform.repository.RoleRepository;
import com.example.audioplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(RoleRepository roleRepository,
                           GenreRepository genreRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.bootstrap.admin-username}") String adminUsername,
                           @Value("${app.bootstrap.admin-email}") String adminEmail,
                           @Value("${app.bootstrap.admin-password}") String adminPassword) {
        this.roleRepository = roleRepository;
        this.genreRepository = genreRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

        List.of("Rock", "Pop", "Rap", "Electronic", "Classical", "Podcast", "Other")
                .forEach(name -> genreRepository.findByName(name)
                        .orElseGet(() -> genreRepository.save(new Genre(name))));

        if (userRepository.findByUsername(adminUsername).isEmpty()
                && userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(adminRole);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setEnabled(true);
            userRepository.save(admin);
        }

        if (userRole.getId() == null) {
            roleRepository.save(userRole);
        }
    }
}
