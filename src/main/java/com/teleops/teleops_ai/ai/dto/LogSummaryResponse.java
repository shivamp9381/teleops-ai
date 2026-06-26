package com.teleops.teleops_ai.ai.dto;

import java.time.LocalDateTime;

/**
 * Log Summary Response DTO
 *
 * Not persisted to database.
 * Returned directly to the client.
 *
 * summary: plain English explanation of what logs show
 * keyEvents: list of notable events found in logs
 * severity: AI assessment of how serious the log content is
 */
public class LogSummaryResponse {

    private String summary;
    private String keyEvents;
    private String severity;
    private LocalDateTime analyzedAt;

    // ─────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────

    public LogSummaryResponse() {
    }

    public LogSummaryResponse(String summary, String keyEvents,
                              String severity) {
        this.summary = summary;
        this.keyEvents = keyEvents;
        this.severity = severity;
        this.analyzedAt = LocalDateTime.now();
    }

    // ─────────────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────────────

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getKeyEvents() {
        return keyEvents;
    }

    public void setKeyEvents(String keyEvents) {
        this.keyEvents = keyEvents;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
}