package com.teleops.teleops_ai.alarm.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Alarm MongoDB Document
 *
 * Maps to the "alarms" collection in MongoDB.
 *
 * Key design decisions:
 *
 * 1. deviceId + deviceName (denormalized):
 *    We store both so alarm history shows device name
 *    even if the device is deleted later.
 *
 * 2. raisedBy + resolvedBy:
 *    Track which user raised and resolved the alarm.
 *    Critical for audit and accountability.
 *
 * 3. rcaResult (embedded):
 *    AI analysis result stored directly in the alarm document.
 *    Null until RCA is triggered (Phase 10).
 *
 * 4. raisedAt / resolvedAt:
 *    Two separate timestamps allow calculating
 *    Mean Time To Resolve (MTTR) — a key NOC KPI.
 *
 * Indexes:
 *   status  → frequent filter: "show all ACTIVE alarms"
 *   severity → frequent filter: "show all CRITICAL alarms"
 *   deviceId → frequent join: "show alarms for this device"
 *   raisedAt → time-range queries for reporting
 */
@Document(collection = "alarms")
public class Alarm {

    @Id
    private String id;

    private String title;

    private String description;

    @Indexed
    private AlarmSeverity severity;

    @Indexed
    private AlarmStatus status;

    /**
     * Reference to the device that triggered this alarm.
     * Indexed for fast lookup of "all alarms for device X".
     */
    @Indexed
    private String deviceId;

    /**
     * Denormalized device name for display without joins.
     */
    private String deviceName;

    /**
     * User ID of engineer who raised the alarm.
     */
    private String raisedBy;

    /**
     * User ID of engineer who resolved the alarm.
     * Null until resolved.
     */
    private String resolvedBy;

    /**
     * AI Root Cause Analysis result.
     * Null until RCA is triggered.
     * Populated in Phase 10 by AiService.
     */
    private RcaResult rcaResult;

    /**
     * When the alarm condition started.
     * Set at creation time.
     */
    @Indexed
    private LocalDateTime raisedAt;

    /**
     * When the alarm was resolved.
     * Null until status becomes RESOLVED.
     */
    private LocalDateTime resolvedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    public Alarm() {
    }

    public Alarm(String title, String description,
                 AlarmSeverity severity, String deviceId,
                 String deviceName, String raisedBy) {
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.status = AlarmStatus.ACTIVE;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.raisedBy = raisedBy;
        this.raisedAt = LocalDateTime.now();
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AlarmSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlarmSeverity severity) {
        this.severity = severity;
    }

    public AlarmStatus getStatus() {
        return status;
    }

    public void setStatus(AlarmStatus status) {
        this.status = status;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getRaisedBy() {
        return raisedBy;
    }

    public void setRaisedBy(String raisedBy) {
        this.raisedBy = raisedBy;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public RcaResult getRcaResult() {
        return rcaResult;
    }

    public void setRcaResult(RcaResult rcaResult) {
        this.rcaResult = rcaResult;
    }

    public LocalDateTime getRaisedAt() {
        return raisedAt;
    }

    public void setRaisedAt(LocalDateTime raisedAt) {
        this.raisedAt = raisedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
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
}