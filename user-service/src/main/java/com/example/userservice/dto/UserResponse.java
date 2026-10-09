package com.example.userservice.dto;

import com.example.userservice.model.User;

/**
 * Public representation of a user, never exposes the password hash
 */
public record UserResponse(Long id, String username, String email, String role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }
}
