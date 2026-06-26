package com.teleops.teleops_ai.alarm.dto;

import com.teleops.teleops_ai.alarm.model.Alarm;
import com.teleops.teleops_ai.alarm.model.AlarmSeverity;
import com.teleops.teleops_ai.alarm.model.AlarmStatus;
import com.teleops.teleops_ai.alarm.model.RcaResult;

import java.time.LocalDateTime;

/**
 * Alarm Response DTO
 *
 * What we return to clients for alarm data.
 *
 * Includes:
 *   - All alarm fields
 *   - rcaResult (null until AI analysis is triggered)
 *   - Device info (denormalized for display)
 *
 * Static factory method fromAlarm() handles the conversion.
 * No extra mapping library needed.
 */
public class AlarmResponse {

    private String id;
    private String title;
    private String description;
    private AlarmSeverity severity;
    private AlarmStatus status;
    private String deviceId;
    private String deviceName;
    private String raisedBy;
    private String resolvedBy;
    private RcaResult rcaResult;
    private LocalDateTime raisedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────────
    // Static Factory Method
    // ─────────────────────────────────────────

    public static AlarmResponse fromAlarm(Alarm alarm) {
        AlarmResponse response = new AlarmResponse();
        response.setId(alarm.getId());
        response.setTitle(alarm.getTitle());
        response.setDescription(alarm.getDescription());
        response.setSeverity(alarm.getSeverity());
        response.setStatus(alarm.getStatus());
        response.setDeviceId(alarm.getDeviceId());
        response.setDeviceName(alarm.getDeviceName());
        response.setRaisedBy(alarm.getRaisedBy());
        response.setResolvedBy(alarm.getResolvedBy());
        response.setRcaResult(alarm.getRcaResult());
        response.setRaisedAt(alarm.getRaisedAt());
        response.setResolvedAt(alarm.getResolvedAt());
        response.setCreatedAt(alarm.getCreatedAt());
        response.setUpdatedAt(alarm.getUpdatedAt());
        return response;
    }

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    public AlarmResponse() {
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