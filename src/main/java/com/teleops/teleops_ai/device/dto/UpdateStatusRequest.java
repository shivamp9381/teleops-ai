package com.teleops.teleops_ai.device.dto;

import com.teleops.teleops_ai.device.model.DeviceStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Update Status Request DTO
 *
 * Used for PATCH /devices/{id}/status
 *
 * Why a separate DTO for status update?
 *   Status updates are a very common NOC operation.
 *   Engineers update status frequently during incidents.
 *   We do not want them to send the full device object
 *   just to change one field.
 *
 *   PATCH semantics = partial update (just status).
 *   PUT semantics   = full replacement (all fields).
 */
public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private DeviceStatus status;

    public UpdateStatusRequest() {
    }

    public UpdateStatusRequest(DeviceStatus status) {
        this.status = status;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }
}