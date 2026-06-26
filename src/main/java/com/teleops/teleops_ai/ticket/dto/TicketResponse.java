package com.teleops.teleops_ai.ticket.dto;

import com.teleops.teleops_ai.ticket.model.IncidentReport;
import com.teleops.teleops_ai.ticket.model.Ticket;
import com.teleops.teleops_ai.ticket.model.TicketPriority;
import com.teleops.teleops_ai.ticket.model.TicketStatus;

import java.time.LocalDateTime;

/**
 * Ticket Response DTO
 *
 * Complete ticket data returned to clients.
 *
 * Includes all fields including:
 *   - incidentReport (null until AI generates it)
 *   - All timestamps for MTTR calculation on frontend
 *   - Denormalized device and assignee names
 *
 * Static factory method fromTicket() for clean conversion.
 */
public class TicketResponse {

    private String id;
    private String ticketNumber;
    private String title;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private String alarmId;
    private String deviceId;
    private String deviceName;
    private String assignedTo;
    private String assignedName;
    private String createdBy;
    private String resolution;
    private IncidentReport incidentReport;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────────
    // Static Factory Method
    // ─────────────────────────────────────────

    public static TicketResponse fromTicket(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setTicketNumber(ticket.getTicketNumber());
        response.setTitle(ticket.getTitle());
        response.setDescription(ticket.getDescription());
        response.setStatus(ticket.getStatus());
        response.setPriority(ticket.getPriority());
        response.setAlarmId(ticket.getAlarmId());
        response.setDeviceId(ticket.getDeviceId());
        response.setDeviceName(ticket.getDeviceName());
        response.setAssignedTo(ticket.getAssignedTo());
        response.setAssignedName(ticket.getAssignedName());
        response.setCreatedBy(ticket.getCreatedBy());
        response.setResolution(ticket.getResolution());
        response.setIncidentReport(ticket.getIncidentReport());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setResolvedAt(ticket.getResolvedAt());
        response.setClosedAt(ticket.getClosedAt());
        response.setUpdatedAt(ticket.getUpdatedAt());
        return response;
    }

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    public TicketResponse() {
    }

    // ─────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
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

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public String getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(String alarmId) {
        this.alarmId = alarmId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getAssignedName() {
        return assignedName;
    }

    public void setAssignedName(String assignedName) {
        this.assignedName = assignedName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public IncidentReport getIncidentReport() {
        return incidentReport;
    }

    public void setIncidentReport(IncidentReport incidentReport) {
        this.incidentReport = incidentReport;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}