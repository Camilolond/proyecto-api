package com.example.userservice.controller;

import com.example.userservice.dto.UpdateUserRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Responds to a GET /api/users call, ADMIN only
     * @return Complete list of users as JSON
     */
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers().stream().map(UserResponse::from).toList();
    }

    /**
     * Responds to a GET /api/users/me call
     * @param jwt Token of the caller
     * @return The user that owns the token
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return userService.getUserById(userIdOf(jwt))
                .map(UserResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Responds to a GET /api/users/{id} call, only the owner or an ADMIN can see it
     * @param id ID of the user
     * @return 200 OK with the user, 403 Forbidden if the caller isnt allowed, 404 Not Found if the id doesnt exist
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        if (!isOwnerOrAdmin(id, jwt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return userService.getUserById(id)
                .map(UserResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Responds to a PUT /api/users/{id} call, only the owner or an ADMIN can update it
     * @param id ID of the user to update
     * @param request New email and/or password
     * @return 200 OK with the updated user, 403 Forbidden if the caller isnt allowed, 404 Not Found if the id doesnt exist
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request,
                                                   @AuthenticationPrincipal Jwt jwt) {
        if (!isOwnerOrAdmin(id, jwt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return userService.updateUser(id, request)
                .map(UserResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Responds to a DELETE /api/users/{id} call, ADMIN only
     * @param id ID of the user to delete
     * @return 204 No Content if the user got deleted, 404 Not Found if the user doesnt exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private Long userIdOf(Jwt jwt) {
        return ((Number) jwt.getClaim("userId")).longValue();
    }

    private boolean isOwnerOrAdmin(Long id, Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return id.equals(userIdOf(jwt)) || (roles != null && roles.contains("ADMIN"));
    }
}
