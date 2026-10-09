package com.example.authservice.dto;

/**
 * @param accessToken Signed JWT to send in the Authorization: Bearer header
 * @param tokenType Always "Bearer"
 * @param expiresIn Seconds until the token expires
 */
public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
}
