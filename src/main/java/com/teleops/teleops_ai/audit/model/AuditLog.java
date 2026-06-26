package com.teleops.teleops_ai.audit.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Audit Log MongoDB Document
 *
 * Maps to the "audit_logs" collection.
 *
 * Every mutating action in the system creates one record.
 * Read operations (GET) are NOT audited — too noisy.
 *
 * What we capture:
 *   userEmail     = who performed the action
 *   action        = what they did
 *   resourceType  = what type of resource (Device, Alarm, etc.)
 *   resourceId    = which specific resource
 *   resourceName  = human-readable name (for display without join)
 *   oldValue      = before state (JSON string, nullable)
 *   newValue      = after state (JSON string, nullable)
 *   ipAddress     = where the request came from
 *   details       = free text for additional context
 *   timestamp     = when it happened
 *
 * Indexes:
 *   userEmail  = "show all actions by this user"
 *   action     = "show all logins" etc.
 *   timestamp  = time-range queries
 *   resourceId = "show all actions on this device"
 */
@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id;

    @Indexed
    private String userEmail;

    @Indexed
    private AuditAction action;

    private String resourceType;

    @Indexed
    private String resourceId;

    private String resourceName;

    /**
     * Previous state as JSON string.
     * Null for CREATE actions.
     */
    private String oldValue;

    /**
     * New state as JSON string.
     * Null for DELETE actions.
     */
    private String newValue;

    /**
     * IP address of the client making the request.
     */
    private String ipAddress;

    /**
     * Additional human-readable context.
     * Example: "Status changed from ONLINE to OFFLINE"
     */
    private String details;

    @Indexed
    private LocalDateTime timestamp;

    // ─────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────

    public AuditLog() {
    }

    public static AuditLog of(String userEmail, AuditAction action,
                              String resourceType, String resourceId,
                              String resourceName, String details) {
        AuditLog log = new AuditLog();
        log.setUserEmail(userEmail);
        log.setAction(action);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setResourceName(resourceName);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        return log;
    }

    // ─────────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public AuditAction getAction() { return action; }
    public void setAction(AuditAction action) { this.action = action; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}