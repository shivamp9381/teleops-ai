package com.teleops.teleops_ai.device.dto;

import com.teleops.teleops_ai.device.model.DeviceStatus;
import com.teleops.teleops_ai.device.model.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Device Request DTO
 *
 * Used for both Create (POST) and Update (PUT) operations.
 *
 * Design decision: One DTO for create and update.
 *   Pros: Less code duplication
 *   Cons: Update might not require all fields
 *   Decision: Acceptable here — device updates always
 *             send the full device object.
 *
 * IP address validation:
 *   We use a regex pattern to ensure valid IPv4 format.
 *   Format: 0-255.0-255.0-255.0-255
 */
public class DeviceRequest {

    @NotBlank(message = "Device name is required")
    @Size(min = 2, max = 100,
            message = "Device name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Device type is required")
    private DeviceType type;

    @NotNull(message = "Device status is required")
    private DeviceStatus status;

    @NotBlank(message = "IP address is required")
    @Pattern(
            regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
                    "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
            message = "IP address must be a valid IPv4 address"
    )
    private String ipAddress;

    @NotBlank(message = "Location is required")
    @Size(max = 100, message = "Location cannot exceed 100 characters")
    private String location;

    @Size(max = 100, message = "Vendor cannot exceed 100 characters")
    private String vendor;

    @Size(max = 100, message = "Model cannot exceed 100 characters")
    private String model;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    public DeviceRequest() {
    }

    // ─────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────

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
}