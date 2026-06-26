package com.teleops.teleops_ai.device.controller;

import com.teleops.teleops_ai.common.response.ApiResponse;
import com.teleops.teleops_ai.device.dto.DeviceHealthScore;
import com.teleops.teleops_ai.device.dto.DeviceRequest;
import com.teleops.teleops_ai.device.dto.DeviceResponse;
import com.teleops.teleops_ai.device.dto.UpdateStatusRequest;
import com.teleops.teleops_ai.device.model.DeviceStatus;
import com.teleops.teleops_ai.device.model.DeviceType;
import com.teleops.teleops_ai.device.service.DeviceHealthService;
import com.teleops.teleops_ai.device.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Device Controller
 *
 * REST endpoints for device management.
 *
 * Access control using @PreAuthorize:
 *
 *   POST   /devices       = SUPER_ADMIN, NOC_MANAGER only
 *   GET    /devices       = all authenticated users
 *   GET    /devices/{id}  = all authenticated users
 *   PUT    /devices/{id}  = SUPER_ADMIN, NOC_MANAGER only
 *   DELETE /devices/{id}  = SUPER_ADMIN only
 *   PATCH  /devices/{id}/status = SUPER_ADMIN, NOC_MANAGER, NOC_ENGINEER
 *   GET    /devices/search = all authenticated users
 *
 * @PreAuthorize expressions:
 *   hasRole('X')               = user must have role X
 *   hasAnyRole('X', 'Y')       = user must have role X or Y
 *
 * Note: Spring Security prepends ROLE_ automatically.
 * hasRole('NOC_MANAGER') checks for ROLE_NOC_MANAGER authority.
 */
@RestController
@RequestMapping("/api/v1/devices")
@Tag(name = "Device Management",
        description = "Manage 4G/5G towers, routers, firewalls, switches and base stations")
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceHealthService  deviceHealthService;


    public DeviceController(DeviceService deviceService, DeviceHealthService deviceHealthService) {

        this.deviceService = deviceService;
        this.deviceHealthService = deviceHealthService;
    }

    /**
     * POST /api/v1/devices
     * Create a new network device.
     * Restricted to SUPER_ADMIN and NOC_MANAGER.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NOC_MANAGER')")
    @Operation(
            summary = "Create device",
            description = "Add a new network device to the NOC inventory."
    )
    public ResponseEntity<ApiResponse<DeviceResponse>> createDevice(
            @Valid @RequestBody DeviceRequest request) {

        DeviceResponse response = deviceService.createDevice(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Device created successfully.", response));
    }

    /**
     * GET /api/v1/devices
     * Get all devices.
     * All authenticated users can view devices.
     */
    @GetMapping
    @Operation(
            summary = "Get all devices",
            description = "Retrieve the complete list of network devices."
    )
    public ResponseEntity<ApiResponse<List<DeviceResponse>>> getAllDevices() {

        List<DeviceResponse> devices = deviceService.getAllDevices();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Devices retrieved successfully.", devices));
    }

    /**
     * GET /api/v1/devices/search
     * Search devices by type, status, or location.
     *
     * NOTE: This endpoint MUST be declared BEFORE /{id}
     * Otherwise Spring will try to interpret "search"
     * as the {id} path variable.
     *
     * Query parameters (all optional):
     *   type     = TOWER_4G, TOWER_5G, ROUTER, etc.
     *   status   = ONLINE, OFFLINE, DEGRADED, MAINTENANCE
     *   location = partial or full location name
     */
    @GetMapping("/search")
    @Operation(
            summary = "Search devices",
            description = "Search devices by type, status, and/or location."
    )
    public ResponseEntity<ApiResponse<List<DeviceResponse>>> searchDevices(
            @RequestParam(required = false) DeviceType type,
            @RequestParam(required = false) DeviceStatus status,
            @RequestParam(required = false) String location) {

        List<DeviceResponse> devices =
                deviceService.searchDevices(type, status, location);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Search completed successfully.", devices));
    }

    /**
     * GET /api/v1/devices/{id}
     * Get a single device by ID.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get device by ID",
            description = "Retrieve a single device by its unique ID."
    )
    public ResponseEntity<ApiResponse<DeviceResponse>> getDeviceById(
            @PathVariable String id) {

        DeviceResponse response = deviceService.getDeviceById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Device retrieved successfully.", response));
    }

    /**
     * PUT /api/v1/devices/{id}
     * Full update of a device.
     * Restricted to SUPER_ADMIN and NOC_MANAGER.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NOC_MANAGER')")
    @Operation(
            summary = "Update device",
            description = "Full update of a device. All fields must be provided."
    )
    public ResponseEntity<ApiResponse<DeviceResponse>> updateDevice(
            @PathVariable String id,
            @Valid @RequestBody DeviceRequest request) {

        DeviceResponse response = deviceService.updateDevice(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Device updated successfully.", response));
    }

    /**
     * PATCH /api/v1/devices/{id}/status
     * Update only the device status.
     * Engineers can update status (common during incidents).
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NOC_MANAGER', 'NOC_ENGINEER')")
    @Operation(
            summary = "Update device status",
            description = "Update only the operational status of a device."
    )
    public ResponseEntity<ApiResponse<DeviceResponse>> updateDeviceStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateStatusRequest request) {

        DeviceResponse response =
                deviceService.updateDeviceStatus(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Device status updated successfully.", response));
    }

    /**
     * DELETE /api/v1/devices/{id}
     * Delete a device.
     * Restricted to SUPER_ADMIN only.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Delete device",
            description = "Permanently remove a device from the NOC inventory."
    )
    public ResponseEntity<ApiResponse<Void>> deleteDevice(
            @PathVariable String id) {

        deviceService.deleteDevice(id);

        return ResponseEntity.ok(
                ApiResponse.success("Device deleted successfully."));
    }


    /**
     * GET /api/v1/devices/{id}/health
     * Get health score for one device.
     */
    @GetMapping("/{id}/health")
    @Operation(summary = "Get device health score",
            description = "Returns 0-100 health score with grade.")
    public ResponseEntity<ApiResponse<DeviceHealthScore>> getDeviceHealth(
            @PathVariable String id) {

        DeviceHealthScore score = deviceHealthService.calculateHealth(id);
        return ResponseEntity.ok(
                ApiResponse.success("Health score calculated.", score));
    }

    /**
     * GET /api/v1/devices/health/all
     * Get health scores for all devices, worst first.
     */
    @GetMapping("/health/all")
    @Operation(summary = "Get health scores for all devices")
    public ResponseEntity<ApiResponse<List<DeviceHealthScore>>> getAllHealth() {

        List<DeviceHealthScore> scores =
                deviceHealthService.calculateAllHealthScores();
        return ResponseEntity.ok(
                ApiResponse.success("All health scores calculated.", scores));
    }
}