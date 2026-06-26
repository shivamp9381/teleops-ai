package com.teleops.teleops_ai.ai.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Incident Report Request DTO
 *
 * Manager sends the ticket ID.
 * Service fetches full ticket + linked alarm data.
 * AI generates a structured 5-section report.
 * Report is saved to ticket.incidentReport.
 *
 * Business rule enforced in service:
 *   Only RESOLVED or CLOSED tickets can have
 *   an incident report generated.
 *   OPEN or IN_PROGRESS tickets do not have
 *   enough resolution data for a useful report.
 */
public class IncidentReportRequest {

    @NotBlank(message = "Ticket ID is required")
    private String ticketId;

    // ─────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────

    public IncidentReportRequest() {
    }

    public IncidentReportRequest(String ticketId) {
        this.ticketId = ticketId;
    }

    // ─────────────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────────────

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }
}