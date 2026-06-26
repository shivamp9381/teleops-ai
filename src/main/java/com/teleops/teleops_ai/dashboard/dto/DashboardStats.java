package com.teleops.teleops_ai.dashboard.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Dashboard Statistics DTO
 *
 * Aggregated NOC overview displayed on the main dashboard.
 *
 * Why implement Serializable?
 *   When using Redis as a cache store, Spring needs to
 *   serialize this object to store it and deserialize it
 *   to retrieve it. With our JSON serializer (configured
 *   in RedisConfig), Serializable is not strictly required,
 *   but it is a best practice for cache objects.
 *
 * This object is built by DashboardService by querying
 * all module repositories and combining the results.
 *
 * It is then cached in Redis for 60 seconds.
 * All 10 engineers hitting the dashboard get the cached
 * version during that 60-second window.
 */
public class DashboardStats implements Serializable {

    // ─────────────────────────────────────────────
    // Device Statistics
    // ─────────────────────────────────────────────

    private long totalDevices;
    private long onlineDevices;
    private long offlineDevices;
    private long degradedDevices;
    private long maintenanceDevices;

    // ─────────────────────────────────────────────
    // Alarm Statistics
    // ─────────────────────────────────────────────

    private long totalActiveAlarms;
    private long criticalAlarms;
    private long highAlarms;
    private long mediumAlarms;
    private long lowAlarms;

    // ─────────────────────────────────────────────
    // Ticket Statistics
    // ─────────────────────────────────────────────

    private long openTickets;
    private long inProgressTickets;

    public double getMttrHours() {
        return mttrHours;
    }

    public void setMttrHours(double mttrHours) {
        this.mttrHours = mttrHours;
    }

    public long getCriticalLast24h() {
        return criticalLast24h;
    }

    public void setCriticalLast24h(long criticalLast24h) {
        this.criticalLast24h = criticalLast24h;
    }

    public String getAvgRcaTime() {
        return avgRcaTime;
    }

    public void setAvgRcaTime(String avgRcaTime) {
        this.avgRcaTime = avgRcaTime;
    }

    private long resolvedTickets;
    private long closedTickets;
    private long totalTickets;

    private double mttrHours;           // Mean Time To Resolve
    private long criticalLast24h;       // Critical alarms in last 24h
    private String avgRcaTime;          // Average AI RCA time

// Add getters/setters for each

    // ─────────────────────────────────────────────
    // Meta
    // ─────────────────────────────────────────────

    /**
     * When these stats were last computed.
     * Displayed on dashboard so engineers know data freshness.
     */
    private LocalDateTime generatedAt;

    // ─────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────

    public DashboardStats() {
        this.generatedAt = LocalDateTime.now();
    }

    // ─────────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────────

    public long getTotalDevices() {
        return totalDevices;
    }

    public void setTotalDevices(long totalDevices) {
        this.totalDevices = totalDevices;
    }

    public long getOnlineDevices() {
        return onlineDevices;
    }

    public void setOnlineDevices(long onlineDevices) {
        this.onlineDevices = onlineDevices;
    }

    public long getOfflineDevices() {
        return offlineDevices;
    }

    public void setOfflineDevices(long offlineDevices) {
        this.offlineDevices = offlineDevices;
    }

    public long getDegradedDevices() {
        return degradedDevices;
    }

    public void setDegradedDevices(long degradedDevices) {
        this.degradedDevices = degradedDevices;
    }

    public long getMaintenanceDevices() {
        return maintenanceDevices;
    }

    public void setMaintenanceDevices(long maintenanceDevices) {
        this.maintenanceDevices = maintenanceDevices;
    }

    public long getTotalActiveAlarms() {
        return totalActiveAlarms;
    }

    public void setTotalActiveAlarms(long totalActiveAlarms) {
        this.totalActiveAlarms = totalActiveAlarms;
    }

    public long getCriticalAlarms() {
        return criticalAlarms;
    }

    public void setCriticalAlarms(long criticalAlarms) {
        this.criticalAlarms = criticalAlarms;
    }

    public long getHighAlarms() {
        return highAlarms;
    }

    public void setHighAlarms(long highAlarms) {
        this.highAlarms = highAlarms;
    }

    public long getMediumAlarms() {
        return mediumAlarms;
    }

    public void setMediumAlarms(long mediumAlarms) {
        this.mediumAlarms = mediumAlarms;
    }

    public long getLowAlarms() {
        return lowAlarms;
    }

    public void setLowAlarms(long lowAlarms) {
        this.lowAlarms = lowAlarms;
    }

    public long getOpenTickets() {
        return openTickets;
    }

    public void setOpenTickets(long openTickets) {
        this.openTickets = openTickets;
    }

    public long getInProgressTickets() {
        return inProgressTickets;
    }

    public void setInProgressTickets(long inProgressTickets) {
        this.inProgressTickets = inProgressTickets;
    }

    public long getResolvedTickets() {
        return resolvedTickets;
    }

    public void setResolvedTickets(long resolvedTickets) {
        this.resolvedTickets = resolvedTickets;
    }

    public long getClosedTickets() {
        return closedTickets;
    }

    public void setClosedTickets(long closedTickets) {
        this.closedTickets = closedTickets;
    }

    public long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}