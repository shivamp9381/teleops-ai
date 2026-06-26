package com.teleops.teleops_ai.ticket.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Assign Ticket Request DTO
 *
 * Used for PATCH /api/v1/tickets/{id}/assign
 *
 * Only MANAGER+ can assign tickets.
 *
 * We require assignedToUserId:
 *   The ID of the engineer being assigned.
 *   The service validates this user exists
 *   and has the NOC_ENGINEER role.
 *
 * We store assignedName (denormalized) after looking up
 * the user from the users collection.
 */
public class AssignTicketRequest {

    @NotBlank(message = "Assignee user ID is required")
    private String assignedToUserId;

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    public AssignTicketRequest() {
    }

    public AssignTicketRequest(String assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }

    // ─────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────

    public String getAssignedToUserId() {
        return assignedToUserId;
    }

    public void setAssignedToUserId(String assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }
}