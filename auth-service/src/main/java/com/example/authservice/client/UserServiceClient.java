package com.example.authservice.client;

import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.dto.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/**
 * HTTP client for the /internal endpoints of user-service
 */
@Component
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(@Value("${user-service.url}") String baseUrl,
                             @Value("${internal.api-key}") String internalApiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Internal-Key", internalApiKey)
                .build();
    }

    /**
     * Asks user-service to check the credentials
     * @return The user if the credentials are valid, empty if user-service answers 401
     */
    public Optional<UserDto> verifyCredentials(LoginRequest request) {
        try {
            UserDto user = restClient.post()
                    .uri("/internal/users/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(UserDto.class);
            return Optional.ofNullable(user);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().isSameCodeAs(HttpStatus.UNAUTHORIZED)) {
                return Optional.empty();
            }
            throw e;
        }
    }

    /**
     * Asks user-service to create a new user
     * @return The created user
     * @throws HttpClientErrorException with 400 if a field is missing or 409 if the username is taken
     */
    public UserDto createUser(RegisterRequest request) {
        return restClient.post()
                .uri("/internal/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(UserDto.class);
    }
}
