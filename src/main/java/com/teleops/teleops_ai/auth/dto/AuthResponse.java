package com.teleops.teleops_ai.auth.dto;

import com.teleops.teleops_ai.auth.model.Role;

/**
 * Authentication Response DTO
 *
 * What we return to the client after successful login or register.
 *
 * We return:
 *   - accessToken:  Used for all API calls (15 min expiry)
 *   - refreshToken: Used to get new access token (7 day expiry)
 *   - tokenType:    Always "Bearer" — standard OAuth2 convention
 *   - user info:    So the frontend can display the user's name and role
 *                   without making an extra API call
 *
 * We do NOT return the password hash. Ever.
 */
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";

    // User info embedded so frontend does not need extra /me call
    private String userId;
    private String name;
    private String email;
    private Role role;

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String refreshToken,
                        String userId, String name,
                        String email, Role role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // ─────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
}