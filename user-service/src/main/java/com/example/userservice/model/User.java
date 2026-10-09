package com.example.userservice.model;

public class User {

    private Long id;
    private String username;
    private String email;
    private String password;
    private String role;

    public User() {
    }

    /**
     * User constructor method
     * @param id ID of the user, not specified by the client
     * @param username Unique username used to log in
     * @param email Email of the user
     * @param password BCrypt hash of the password, never the plain text
     * @param role Role of the user (USER or ADMIN)
     */
    public User(Long id, String username, String email, String password, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
