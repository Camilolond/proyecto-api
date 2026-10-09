package com.example.userservice.service;

import com.example.userservice.dto.CreateUserRequest;
import com.example.userservice.dto.UpdateUserRequest;
import com.example.userservice.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    private final List<User> users = new CopyOnWriteArrayList<>();
    private final AtomicLong counter = new AtomicLong();
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor method, preloads an admin (admin/admin123) and a regular user (user/user123) for testing
     */
    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        users.add(new User(counter.incrementAndGet(), "admin", "admin@example.com", passwordEncoder.encode("admin123"), "ADMIN"));
        users.add(new User(counter.incrementAndGet(), "user", "user@example.com", passwordEncoder.encode("user123"), "USER"));
    }

    /**
     * Used by the controller in the endpoint GET /api/users
     * @return The whole list of users
     */
    public List<User> getAllUsers() {
        return users;
    }

    /**
     * @param id ID of the user
     * @return The user with the matching ID
     */
    public Optional<User> getUserById(Long id) {
        return users.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst();
    }

    /**
     * @param username Username to search, case insensitive
     * @return The user with the matching username
     */
    public Optional<User> getUserByUsername(String username) {
        return users.stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    /**
     * Creates a user with role USER, hashing the password before storing it
     * @param request Username, email and plain text password
     * @return The created user, empty if the username is already taken
     */
    public synchronized Optional<User> createUser(CreateUserRequest request) {
        if (getUserByUsername(request.username()).isPresent()) {
            return Optional.empty();
        }
        User user = new User(counter.incrementAndGet(), request.username(), request.email(),
                passwordEncoder.encode(request.password()), "USER");
        users.add(user);
        return Optional.of(user);
    }

    /**
     * Overwrites the email and/or password of the user, null fields are ignored
     * @param id ID of the user to update
     * @param request New email and/or plain text password
     * @return The updated user, empty if the user doesnt exist
     */
    public Optional<User> updateUser(Long id, UpdateUserRequest request) {
        return getUserById(id).map(user -> {
            if (request.email() != null) {
                user.setEmail(request.email());
            }
            if (request.password() != null) {
                user.setPassword(passwordEncoder.encode(request.password()));
            }
            return user;
        });
    }

    /**
     * Deletes the specified user by ID
     * @param id ID of the user to delete
     * @return true if the user got deleted
     */
    public boolean deleteUser(Long id) {
        return users.removeIf(user -> user.getId().equals(id));
    }

    /**
     * Checks a username and plain text password against the stored hash
     * @return The user if the credentials are valid, empty otherwise
     */
    public Optional<User> verifyCredentials(String username, String password) {
        return getUserByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()));
    }
}
