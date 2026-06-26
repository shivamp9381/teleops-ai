package com.teleops.teleops_ai.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Update Profile Request DTO
 *
 * Used for PATCH /api/v1/users/me/profile
 *
 * Users can update only their own name.
 * Email changes are not allowed (used as login identifier).
 * Password changes require a separate endpoint (security).
 * Role changes require SUPER_ADMIN.
 */
public class UpdateProfileRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100,
            message = "Name must be between 2 and 100 characters")
    private String name;

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String name) {
        this.name = name;
    }

    // ─────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}