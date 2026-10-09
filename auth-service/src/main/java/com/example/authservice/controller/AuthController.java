package com.example.authservice.controller;

import com.example.authservice.client.UserServiceClient;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.dto.TokenResponse;
import com.example.authservice.dto.UserDto;
import com.example.authservice.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserServiceClient userServiceClient;
    private final JwtService jwtService;

    public AuthController(UserServiceClient userServiceClient, JwtService jwtService) {
        this.userServiceClient = userServiceClient;
        this.jwtService = jwtService;
    }

    /**
     * Responds to a POST /api/auth/register call, creates the user in user-service with role USER
     * @param request Username, email and password
     * @return 201 Created with the user, 400 Bad Request if a field is missing, 409 Conflict if the username is taken
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(@RequestBody RegisterRequest request) {
        return userServiceClient.createUser(request);
    }

    /**
     * Responds to a POST /api/auth/login call
     * @param request Username and password
     * @return 200 OK with a JWT if the credentials are valid, 401 Unauthorized otherwise
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        return userServiceClient.verifyCredentials(request)
                .map(jwtService::generateToken)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    /**
     * Responds to a GET /api/auth/me call, useful to check if a token is still valid
     * @param jwt Token sent in the Authorization header
     * @return The claims of the token
     */
    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getClaims();
    }

    /**
     * Forwards the 4xx status returned by user-service (400, 409) to the client
     */
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Void> handleUserServiceError(HttpClientErrorException e) {
        return ResponseEntity.status(e.getStatusCode()).build();
    }

    /**
     * user-service is down or unreachable
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Void> handleUserServiceUnavailable() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
