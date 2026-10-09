package com.example.userservice.dto;

/**
 * Fields left as null are not modified
 */
public record UpdateUserRequest(String email, String password) {
}
