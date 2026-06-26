package com.teleops.teleops_ai.alarm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Resolve Alarm Request DTO
 *
 * Used when resolving an alarm via PATCH /api/v1/alarms/{id}/resolve
 *
 * Why require a resolution note?
 *   Engineers MUST document what they did to fix the issue.
 *   This is critical for:
 *     - Building a knowledge base of fixes
 *     - AI incident report generation (Phase 10)
 *     - Audit and compliance requirements
 *     - Future engineers facing the same issue
 *
 * We enforce minimum 10 characters to prevent
 * lazy "fixed" or "done" resolution notes.
 */
public class ResolveAlarmRequest {

    @NotBlank(message = "Resolution note is required")
    @Size(min = 10, max = 1000,
            message = "Resolution note must be between 10 and 1000 characters")
    private String resolutionNote;

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    public ResolveAlarmRequest() {
    }

    public ResolveAlarmRequest(String resolutionNote) {
        this.resolutionNote = resolutionNote;
    }

    // ─────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────

    public String getResolutionNote() {
        return resolutionNote;
    }

    public void setResolutionNote(String resolutionNote) {
        this.resolutionNote = resolutionNote;
    }
}