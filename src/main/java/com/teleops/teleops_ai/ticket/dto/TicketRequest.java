package com.teleops.teleops_ai.ticket.dto;

import com.teleops.teleops_ai.ticket.model.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Ticket Request DTO
 *
 * Used when creating a new ticket via POST /api/v1/tickets
 *
 * alarmId is optional:
 *   - Provided when ticket is raised from an existing alarm
 *   - Null for standalone tickets (maintenance, proactive work)
 *
 * deviceId is mandatory:
 *   - Every ticket must relate to a specific device
 *   - This is validated against the devices collection
 *
 * We do NOT accept status from the client.
 * New tickets always start as OPEN.
 * The service enforces this.
 *
 * ticketNumber is NOT in this DTO.
 * The service auto-generates it.
 */
public class TicketRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200,
            message = "Title must be between 5 and 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000,
            message = "Description must be between 10 and 2000 characters")
    private String description;

    @NotNull(message = "Priority is required")
    private TicketPriority priority;

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    /**
     * Optional: link to the alarm that triggered this ticket.
     * Null for standalone tickets.
     */
    private String alarmId;

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    public TicketRequest() {
    }

    public TicketRequest(String title, String description,
                         TicketPriority priority, String deviceId,
                         String alarmId) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.deviceId = deviceId;
        this.alarmId = alarmId;
    }

    // ─────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────

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

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(String alarmId) {
        this.alarmId = alarmId;
    }
}