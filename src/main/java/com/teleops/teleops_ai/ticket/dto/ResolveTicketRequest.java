package com.teleops.teleops_ai.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Resolve Ticket Request DTO
 *
 * Used for PATCH /api/v1/tickets/{id}/resolve
 *
 * Resolution note is mandatory — same reasoning as alarms.
 * Engineers must document what they did.
 * This data feeds into the AI incident report generation.
 *
 * Minimum 20 characters enforced to prevent
 * low-quality resolutions like "fixed" or "done".
 */
public class ResolveTicketRequest {

    @NotBlank(message = "Resolution is required")
    @Size(min = 20, max = 2000,
            message = "Resolution must be between 20 and 2000 characters")
    private String resolution;

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    public ResolveTicketRequest() {
    }

    public ResolveTicketRequest(String resolution) {
        this.resolution = resolution;
    }

    // ─────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }
}