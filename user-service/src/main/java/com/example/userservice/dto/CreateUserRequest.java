package com.example.userservice.dto;

public record CreateUserRequest(String username, String email, String password) {
}
