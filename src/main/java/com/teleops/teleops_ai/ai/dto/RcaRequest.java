package com.teleops.teleops_ai.ai.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Root Cause Analysis Request DTO
 *
 * The client sends the alarm ID they want analyzed.
 * The service fetches full alarm and device details
 * from the database before sending to Groq AI.
 *
 * We never accept alarm/device details from the client.
 * Always fetch from our own database to ensure accuracy.
 * A client could send fabricated data otherwise.
 */
public class RcaRequest {

    @NotBlank(message = "Alarm ID is required")
    private String alarmId;

    // ─────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────

    public RcaRequest() {
    }

    public RcaRequest(String alarmId) {
        this.alarmId = alarmId;
    }

    // ─────────────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────────────

    public String getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(String alarmId) {
        this.alarmId = alarmId;
    }
}