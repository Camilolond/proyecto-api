package com.example.authservice.dto;

/**
 * User as returned by user-service
 */
public record UserDto(Long id, String username, String email, String role) {
}
