package com.teleops.teleops_ai.device.dto;

import com.teleops.teleops_ai.device.model.Device;
import com.teleops.teleops_ai.device.model.DeviceStatus;
import com.teleops.teleops_ai.device.model.DeviceType;

import java.time.LocalDateTime;

/**
 * Device Response DTO
 *
 * What we return to clients for device data.
 *
 * This mirrors the Device model but is a separate class because:
 *   - We control exactly what fields are exposed in the API
 *   - If we add internal fields to Device model later,
 *     they are not accidentally exposed
 *   - Response shape can evolve independently of DB model
 *
 * Static factory method fromDevice():
 *   Clean conversion from model to DTO.
 *   No MapStruct needed for this simple mapping.
 */
public class DeviceResponse {

    private String id;
    private String name;
    private DeviceType type;
    private DeviceStatus status;
    private String ipAddress;
    private String location;
    private String vendor;
    private String model;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────────
    // Static Factory Method
    // ─────────────────────────────────────────

    /**
     * Convert Device model to DeviceResponse DTO.
     * Single place for this mapping — easy to maintain.
     */
    public static DeviceResponse fromDevice(Device device) {
        DeviceResponse response = new DeviceResponse();
        response.setId(device.getId());
        response.setName(device.getName());
        response.setType(device.getType());
        response.setStatus(device.getStatus());
        response.setIpAddress(device.getIpAddress());
        response.setLocation(device.getLocation());
        response.setVendor(device.getVendor());
        response.setModel(device.getModel());
        response.setDescription(device.getDescription());
        response.setCreatedAt(device.getCreatedAt());
        response.setUpdatedAt(device.getUpdatedAt());
        return response;
    }

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    public DeviceResponse() {
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