package com.teleops.teleops_ai.auth.dto;

import com.teleops.teleops_ai.auth.model.Role;
import com.teleops.teleops_ai.auth.model.User;

import java.time.LocalDateTime;

/**
 * User Profile Response DTO
 *
 * Returned by GET /api/v1/auth/me
 *
 * Safe version of the User model:
 *   - Includes all displayable fields
 *   - NEVER includes the password field
 *
 * We use a static factory method fromUser() as a clean
 * way to convert from model to DTO without MapStruct.
 */
public class UserResponse {

    private String id;
    private String name;
    private String email;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;

    // ─────────────────────────────────────────
    // Static factory method
    // ─────────────────────────────────────────

    /**
     * Converts a User model into a safe UserResponse DTO.
     * This is the only place where User → UserResponse mapping happens.
     */
    public static UserResponse fromUser(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setActive(user.isActive());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    public UserResponse() {
    }

    // ─────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}