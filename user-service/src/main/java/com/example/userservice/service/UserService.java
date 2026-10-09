package com.example.userservice.service;

import com.example.userservice.dto.CreateUserRequest;
import com.example.userservice.dto.UpdateUserRequest;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Used by the controller in the endpoint GET /api/users
     * @return The whole list of users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll(Sort.by("id"));
    }

    /**
     * @param id ID of the user
     * @return The user with the matching ID
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * @param username Username to search, case insensitive
     * @return The user with the matching username
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username);
    }

    /**
     * Creates a user with role USER, hashing the password before storing it
     * @param request Username, email and plain text password
     * @return The created user, empty if the username is already taken
     */
    public Optional<User> createUser(CreateUserRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            return Optional.empty();
        }
        User user = new User(null, request.username(), request.email(),
                passwordEncoder.encode(request.password()), "USER");
        try {
            return Optional.of(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            // Another request registered the same username at the same time, the unique constraint rejected it
            return Optional.empty();
        }
    }

    /**
     * Overwrites the email and/or password of the user, null fields are ignored
     * @param id ID of the user to update
     * @param request New email and/or plain text password
     * @return The updated user, empty if the user doesnt exist
     */
    @Transactional
    public Optional<User> updateUser(Long id, UpdateUserRequest request) {
        return getUserById(id).map(user -> {
            if (request.email() != null) {
                user.setEmail(request.email());
            }
            if (request.password() != null) {
                user.setPassword(passwordEncoder.encode(request.password()));
            }
            return userRepository.save(user);
        });
    }

    /**
     * Deletes the specified user by ID
     * @param id ID of the user to delete
     * @return true if the user got deleted
     */
    @Transactional
    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
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
