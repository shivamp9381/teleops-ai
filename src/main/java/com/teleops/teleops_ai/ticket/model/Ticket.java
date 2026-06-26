package com.teleops.teleops_ai.ticket.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDateTime;

/**
 * Ticket MongoDB Document
 *
 * Maps to the "tickets" collection in MongoDB.
 *
 * Key design decisions:
 *
 * 1. ticketNumber (unique index):
 *    Auto-generated human-readable ID.
 *    Format: TKT-2024-00001
 *    Engineers reference tickets by this number.
 *
 * 2. alarmId (optional):
 *    Tickets can be linked to an alarm.
 *    Or they can be standalone (maintenance work, etc.)
 *    This field is nullable.
 *
 * 3. deviceId + deviceName (denormalized):
 *    Same pattern as alarms.
 *    Fast reads, preserved history if device deleted.
 *
 * 4. assignedTo + assignedName (denormalized):
 *    Both engineer ID and name stored.
 *    Avoids user lookup on every ticket read.
 *
 * 5. incidentReport (embedded):
 *    AI-generated report. Null until Phase 10.
 *
 * 6. Timestamps:
 *    createdAt  = ticket opened
 *    resolvedAt = fix applied
 *    closedAt   = management closed
 *    Enables MTTR (Mean Time To Resolve) calculation.
 *
 * Indexes:
 *   ticketNumber  → unique, human reference
 *   status        → filter by open/closed
 *   priority      → filter by urgency
 *   assignedTo    → "my tickets" view
 *   deviceId      → device's ticket history
 *   createdAt     → time-range reports
 */
@Document(collection = "tickets")
public class Ticket {

    @Id
    private String id;

    @Indexed(unique = true)
    private String ticketNumber;

    private String title;

    private String description;

    @Indexed
    private TicketStatus status;

    @Indexed
    private com.teleops.teleops_ai.ticket.model.TicketPriority priority;

    /**
     * Optional: linked alarm that triggered this ticket.
     * Null for standalone (non-alarm) tickets.
     */
    private String alarmId;

    /**
     * Device this ticket is about.
     */
    @Indexed
    private String deviceId;

    /**
     * Denormalized for fast display.
     */
    private String deviceName;

    /**
     * User ID of engineer assigned to this ticket.
     * Null until assigned by manager.
     */
    @Indexed
    private String assignedTo;

    /**
     * Denormalized assignee name for display.
     */
    private String assignedName;

    /**
     * User ID who created this ticket.
     */
    private String createdBy;

    /**
     * How the issue was resolved.
     * Required before closing.
     */
    private String resolution;

    /**
     * AI-generated incident report.
     * Null until Phase 10 generates it.
     */
    private IncidentReport incidentReport;

    @Indexed
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    private LocalDateTime closedAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    public Ticket() {
    }

    public Ticket(String ticketNumber, String title, String description,
                  TicketPriority priority, String deviceId,
                  String deviceName, String createdBy, String alarmId) {
        this.ticketNumber = ticketNumber;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = TicketStatus.OPEN;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.createdBy = createdBy;
        this.alarmId = alarmId;
        this.createdAt = LocalDateTime.now();
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