package com.teleops.teleops_ai.auth.dto;

import com.teleops.teleops_ai.auth.model.Role;
import com.teleops.teleops_ai.auth.model.User;

import java.time.LocalDateTime;

/**
 * Extended User Profile Response DTO
 *
 * Rich profile view returned to:
 *   - The user viewing their own profile (GET /api/v1/users/me/profile)
 *   - Managers viewing subordinate profiles
 *   - Super Admin viewing any user
 *
 * Contains activity statistics:
 *   - Total alarms raised
 *   - Total tickets created
 *   - Total tickets resolved
 *
 * These stats are populated by UserService by querying
 * alarm and ticket repositories.
 */
public class UserProfileResponse {

    // Core identity
    private String id;
    private String name;
    private String email;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Activity statistics
    private long alarmsRaised;
    private long ticketsCreated;
    private long ticketsResolved;
    private long ticketsAssigned;

    // ─────────────────────────────────────────
    // Static factory method
    // ─────────────────────────────────────────

    public static UserProfileResponse fromUser(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setActive(user.isActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    public UserProfileResponse() {
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getAlarmsRaised() {
        return alarmsRaised;
    }

    public void setAlarmsRaised(long alarmsRaised) {
        this.alarmsRaised = alarmsRaised;
    }

    public long getTicketsCreated() {
        return ticketsCreated;
    }

    public void setTicketsCreated(long ticketsCreated) {
        this.ticketsCreated = ticketsCreated;
    }

    public long getTicketsResolved() {
        return ticketsResolved;
    }

    public void setTicketsResolved(long ticketsResolved) {
        this.ticketsResolved = ticketsResolved;
    }

    public long getTicketsAssigned() {
        return ticketsAssigned;
    }

    public void setTicketsAssigned(long ticketsAssigned) {
        this.ticketsAssigned = ticketsAssigned;
    }
}