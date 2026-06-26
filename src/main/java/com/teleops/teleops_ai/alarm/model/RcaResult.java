package com.teleops.teleops_ai.alarm.model;

import java.time.LocalDateTime;

/**
 * RCA Result - Embedded MongoDB Document
 *
 * This class is NOT a top-level MongoDB collection.
 * It is embedded INSIDE the Alarm document.
 *
 * In MongoDB terms:
 *   alarms collection document:
 *   {
 *     "_id": "...",
 *     "title": "Tower offline",
 *     ...
 *     "rcaResult": {
 *       "possibleCause": "Power failure at site",
 *       "confidence": "HIGH",
 *       "suggestedFix": "Check power supply unit",
 *       "analyzedAt": "2024-01-15T10:30:00"
 *     }
 *   }
 *
 * Why embed instead of reference?
 *   RCA result belongs to one alarm and is never shared.
 *   Embedding avoids a JOIN (lookup) when displaying alarm details.
 *   This is the MongoDB way.
 *
 * This field is null until AI analysis is triggered.
 * Phase 10 populates this when RCA is requested.
 */
public class RcaResult {

    /**
     * AI-determined probable cause of the alarm.
     * Example: "Possible power outage at the site based on
     * simultaneous loss of multiple services"
     */
    private String possibleCause;

    /**
     * AI confidence in its analysis.
     * Values: HIGH, MEDIUM, LOW
     * Helps engineers weigh the AI suggestion.
     */
    private String confidence;

    /**
     * Recommended action to fix the issue.
     * Example: "Dispatch field engineer to check PDU unit"
     */
    private String suggestedFix;

    /**
     * Timestamp when the AI analysis was performed.
     */
    private LocalDateTime analyzedAt;

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    public RcaResult() {
    }

    public RcaResult(String possibleCause, String confidence,
                     String suggestedFix) {
        this.possibleCause = possibleCause;
        this.confidence = confidence;
        this.suggestedFix = suggestedFix;
        this.analyzedAt = LocalDateTime.now();
    }

    // ─────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────

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