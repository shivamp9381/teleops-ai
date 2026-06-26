package com.teleops.teleops_ai.ai.dto;

import java.time.LocalDateTime;

/**
 * Root Cause Analysis Response DTO
 *
 * Returned to the client after AI analysis.
 *
 * Also contains alarmId so the frontend knows which
 * alarm was analyzed (useful when multiple RCA requests
 * are in-flight).
 *
 * The rcaResult is also saved to the Alarm document
 * in MongoDB so engineers can see it when viewing the alarm.
 */
public class RcaResponse {

    private String alarmId;
    private String possibleCause;
    private String confidence;
    private String suggestedFix;
    private LocalDateTime analyzedAt;

    // ─────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────

    public RcaResponse() {
    }

    public RcaResponse(String alarmId, String possibleCause,
                       String confidence, String suggestedFix) {
        this.alarmId = alarmId;
        this.possibleCause = possibleCause;
        this.confidence = confidence;
        this.suggestedFix = suggestedFix;
        this.analyzedAt = LocalDateTime.now();
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

    public String getPossibleCause() {
        return possibleCause;
    }

    public void setPossibleCause(String possibleCause) {
        this.possibleCause = possibleCause;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public String getSuggestedFix() {
        return suggestedFix;
    }

    public void setSuggestedFix(String suggestedFix) {
        this.suggestedFix = suggestedFix;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
}