package com.teleops.teleops_ai.auth.dto;

import com.teleops.teleops_ai.auth.model.Role;
import jakarta.validation.constraints.NotNull;

/**
 * Update User Role Request DTO
 *
 * Used for PATCH /api/v1/users/{id}/role
 * SUPER_ADMIN only.
 *
 * Why restrict role changes to SUPER_ADMIN?
 *   Role changes are a sensitive security operation.
 *   A manager promoting someone to manager or admin
 *   would be a privilege escalation risk.
 */
public class UpdateUserRoleRequest {

    @NotNull(message = "Role is required")
    private Role role;

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    public UpdateUserRoleRequest() {
    }

    public UpdateUserRoleRequest(Role role) {
        this.role = role;
    }

    // ─────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}