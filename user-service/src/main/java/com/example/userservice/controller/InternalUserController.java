package com.example.userservice.controller;

import com.example.userservice.dto.CreateUserRequest;
import com.example.userservice.dto.CredentialsRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Endpoints used only by auth-service, every call must send the shared key in the X-Internal-Key header
 */
@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserService userService;
    private final byte[] internalApiKey;

    public InternalUserController(UserService userService, @Value("${internal.api-key}") String internalApiKey) {
        this.userService = userService;
        this.internalApiKey = internalApiKey.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Responds to a POST /internal/users call, used by auth-service to register users
     * @return 201 Created with the user, 400 Bad Request if a field is missing, 409 Conflict if the username is taken
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestHeader(value = "X-Internal-Key", required = false) String key,
                                                   @RequestBody CreateUserRequest request) {
        if (!isValidKey(key)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (isBlank(request.username()) || isBlank(request.email()) || isBlank(request.password())) {
            return ResponseEntity.badRequest().build();
        }
        return userService.createUser(request)
                .map(user -> ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user)))
                .orElse(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    /**
     * Responds to a POST /internal/users/verify call, used by auth-service on login
     * @return 200 OK with the user if the credentials are valid, 401 Unauthorized otherwise
     */
    @PostMapping("/verify")
    public ResponseEntity<UserResponse> verifyCredentials(@RequestHeader(value = "X-Internal-Key", required = false) String key,
                                                          @RequestBody CredentialsRequest request) {
        if (!isValidKey(key)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (isBlank(request.username()) || isBlank(request.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return userService.verifyCredentials(request.username(), request.password())
                .map(UserResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    private boolean isValidKey(String key) {
        return key != null && MessageDigest.isEqual(internalApiKey, key.getBytes(StandardCharsets.UTF_8));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
