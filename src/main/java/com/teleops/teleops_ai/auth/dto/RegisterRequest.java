package com.teleops.teleops_ai.auth.dto;

import com.teleops.teleops_ai.auth.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Registration Request DTO
 *
 * This is what the client sends in the request body
 * when calling POST /api/v1/auth/register.
 *
 * Validation annotations:
 *   @NotBlank  = field cannot be null, empty, or whitespace only
 *   @Email     = must be a valid email format
 *   @Size      = enforces min/max character length
 *   @NotNull   = field cannot be null (but can be empty string)
 *
 * The message attribute is returned in the validation error response.
 * Keep messages user-friendly.
 *
 * Why a separate DTO and not use the User model directly?
 *   - User model has fields we never want clients to send (id, createdAt)
 *   - DTO gives us control over exactly what input is accepted
 *   - Separation of concerns: API contract vs database model
 */
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    /**
     * Role is optional on registration.
     * If not provided, defaults to NOC_ENGINEER in the service.
     * SUPER_ADMIN role can only be assigned by another SUPER_ADMIN.
     */
    @NotNull(message = "Role is required")
    private Role role;

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    public RegisterRequest() {
    }

    public RegisterRequest(String name, String email,
                           String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}