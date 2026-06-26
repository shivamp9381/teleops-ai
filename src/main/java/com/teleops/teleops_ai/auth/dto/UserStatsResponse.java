package com.teleops.teleops_ai.auth.dto;

/**
 * User Statistics Response DTO
 *
 * Returned by GET /api/v1/users/stats
 * SUPER_ADMIN only.
 *
 * Provides a quick overview of the user base
 * for the admin dashboard.
 */
public class UserStatsResponse {

    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long totalAdmins;
    private long totalManagers;
    private long totalEngineers;
    private long totalReadOnly;

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    public UserStatsResponse() {
    }

    // ─────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getInactiveUsers() {
        return inactiveUsers;
    }

    public void setInactiveUsers(long inactiveUsers) {
        this.inactiveUsers = inactiveUsers;
    }

    public long getTotalAdmins() {
        return totalAdmins;
    }

    public void setTotalAdmins(long totalAdmins) {
        this.totalAdmins = totalAdmins;
    }

    public long getTotalManagers() {
        return totalManagers;
    }

    public void setTotalManagers(long totalManagers) {
        this.totalManagers = totalManagers;
    }

    public long getTotalEngineers() {
        return totalEngineers;
    }

    public void setTotalEngineers(long totalEngineers) {
        this.totalEngineers = totalEngineers;
    }

    public long getTotalReadOnly() {
        return totalReadOnly;
    }

    public void setTotalReadOnly(long totalReadOnly) {
        this.totalReadOnly = totalReadOnly;
    }
}