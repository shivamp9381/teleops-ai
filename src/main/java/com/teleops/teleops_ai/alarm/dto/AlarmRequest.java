package com.teleops.teleops_ai.alarm.dto;

import com.teleops.teleops_ai.alarm.model.AlarmSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Alarm Request DTO
 *
 * Used when raising a new alarm via POST /api/v1/alarms
 *
 * Who raises alarms?
 *   In a real NOC, alarms are raised automatically by:
 *     - Network monitoring systems (NMS)
 *     - SNMP traps from devices
 *     - Threshold-based monitoring tools
 *
 *   In our application, engineers raise alarms manually
 *   to simulate this process.
 *
 * Fields:
 *   deviceId  = which device is raising the alarm
 *   severity  = how serious is it
 *   title     = short description (shown in alarm list)
 *   description = detailed context
 *
 * We do NOT accept status from the client.
 * New alarms always start as ACTIVE.
 * The service enforces this.
 */
public class AlarmRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotNull(message = "Severity is required")
    private AlarmSeverity severity;

    @NotBlank(message = "Alarm title is required")
    @Size(min = 5, max = 200,
            message = "Title must be between 5 and 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000,
            message = "Description must be between 10 and 1000 characters")
    private String description;

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    public AlarmRequest() {
    }

    public AlarmRequest(String deviceId, AlarmSeverity severity,
                        String title, String description) {
        this.deviceId = deviceId;
        this.severity = severity;
        this.title = title;
        this.description = description;
    }

    // ─────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public AlarmSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlarmSeverity severity) {
        this.severity = severity;
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
}