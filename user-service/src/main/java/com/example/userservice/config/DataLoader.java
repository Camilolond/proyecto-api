package com.example.userservice.config;

import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Preloads an admin (admin/admin123) and a regular user (user/user123) the first time
     * the app starts with an empty database
     */
    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        userRepository.saveAll(List.of(
                new User(null, "admin", "admin@example.com", passwordEncoder.encode("admin123"), "ADMIN"),
                new User(null, "user", "user@example.com", passwordEncoder.encode("user123"), "USER")));
    }
}
