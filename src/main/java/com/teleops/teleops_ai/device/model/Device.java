package com.teleops.teleops_ai.device.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Device MongoDB Document
 *
 * Maps to the "devices" collection in MongoDB.
 *
 * Field decisions:
 *
 *   name       = human-readable identifier (e.g., "Tower-BKK-001")
 *   type       = DeviceType enum stored as String
 *   status     = DeviceStatus enum stored as String
 *   ipAddress  = indexed for quick lookup by IP
 *   location   = city or site name (e.g., "Bangkok", "Site-A")
 *   vendor     = hardware manufacturer (e.g., "Ericsson", "Nokia")
 *   model      = hardware model number
 *   description = free text for additional notes
 *
 * Why @Indexed on ipAddress?
 *   Engineers often search by IP during incidents.
 *   Index makes this lookup O(log n) instead of O(n).
 *
 * Why @Indexed on type and status?
 *   Dashboard queries filter by type and status constantly.
 *   Without indexes, every query is a full collection scan.
 */
@Document(collection = "devices")
public class Device {

    @Id
    private String id;

    private String name;

    private DeviceType type;

    private DeviceStatus status;

    @Indexed
    private String ipAddress;

    @Indexed
    private String location;

    private String vendor;

    private String model;

    private String description;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    public Device() {
    }

    public Device(String name, DeviceType type, DeviceStatus status,
                  String ipAddress, String location,
                  String vendor, String model, String description) {
        this.name = name;
        this.type = type;
        this.status = status;
        this.ipAddress = ipAddress;
        this.location = location;
        this.vendor = vendor;
        this.model = model;
        this.description = description;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}