package com.teleops.teleops_ai.alarm.controller;

import com.teleops.teleops_ai.alarm.dto.AlarmRequest;
import com.teleops.teleops_ai.alarm.dto.AlarmResponse;
import com.teleops.teleops_ai.alarm.dto.ResolveAlarmRequest;
import com.teleops.teleops_ai.alarm.model.AlarmSeverity;
import com.teleops.teleops_ai.alarm.service.AlarmService;
import com.teleops.teleops_ai.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Alarm Controller
 *
 * REST endpoints for the full alarm lifecycle.
 *
 * Access control:
 *   POST   /alarms                  = NOC_ENGINEER+
 *   GET    /alarms                  = all authenticated
 *   GET    /alarms/active           = all authenticated
 *   GET    /alarms/{id}             = all authenticated
 *   GET    /alarms/device/{id}      = all authenticated
 *   PATCH  /alarms/{id}/acknowledge = NOC_ENGINEER+
 *   PATCH  /alarms/{id}/resolve     = NOC_ENGINEER+
 *
 * READ_ONLY users can view all alarms but cannot
 * raise, acknowledge, or resolve them.
 *
 * Important endpoint ordering:
 *   /alarms/active and /alarms/device/{id}
 *   MUST be declared before /alarms/{id}
 *   to prevent "active" being treated as {id}.
 */
@RestController
@RequestMapping("/api/v1/alarms")
@Tag(name = "Alarm Management",
        description = "Raise, acknowledge, and resolve network alarms")
public class AlarmController {

    private final AlarmService alarmService;

    public AlarmController(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    /**
     * POST /api/v1/alarms
     * Raise a new alarm on a device.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NOC_MANAGER', 'NOC_ENGINEER')")
    @Operation(
            summary = "Raise alarm",
            description = "Raise a new alarm on a network device."
    )
    public ResponseEntity<ApiResponse<AlarmResponse>> raiseAlarm(
            @Valid @RequestBody AlarmRequest request) {

        AlarmResponse response = alarmService.raiseAlarm(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Alarm raised successfully.", response));
    }

    /**
     * GET /api/v1/alarms
     * Get all alarms.
     */
    @GetMapping
    @Operation(
            summary = "Get all alarms",
            description = "Retrieve complete alarm history."
    )
    public ResponseEntity<ApiResponse<List<AlarmResponse>>> getAllAlarms() {

        List<AlarmResponse> alarms = alarmService.getAllAlarms();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Alarms retrieved successfully.", alarms));
    }

    /**
     * GET /api/v1/alarms/active
     * Get only currently active alarms.
     * Ordered: most critical first.
     *
     * IMPORTANT: Must be declared BEFORE /{id}
     */
    @GetMapping("/active")
    @Operation(
            summary = "Get active alarms",
            description = "Get all currently active (unresolved) alarms ordered by severity."
    )
    public ResponseEntity<ApiResponse<List<AlarmResponse>>> getActiveAlarms() {

        List<AlarmResponse> alarms = alarmService.getActiveAlarms();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Active alarms retrieved.", alarms));
    }

    /**
     * GET /api/v1/alarms/device/{deviceId}
     * Get all alarms for a specific device.
     *
     * IMPORTANT: Must be declared BEFORE /{id}
     */
    @GetMapping("/device/{deviceId}")
    @Operation(
            summary = "Get alarms by device",
            description = "Get full alarm history for a specific device."
    )
    public ResponseEntity<ApiResponse<List<AlarmResponse>>> getAlarmsByDevice(
            @PathVariable String deviceId) {

        List<AlarmResponse> alarms =
                alarmService.getAlarmsByDevice(deviceId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Device alarms retrieved.", alarms));
    }

    /**
     * GET /api/v1/alarms/severity/{severity}
     * Get alarms filtered by severity level.
     */
    @GetMapping("/severity/{severity}")
    @Operation(
            summary = "Get alarms by severity",
            description = "Filter alarms by severity: CRITICAL, HIGH, MEDIUM, LOW"
    )
    public ResponseEntity<ApiResponse<List<AlarmResponse>>> getAlarmsBySeverity(
            @PathVariable AlarmSeverity severity) {

        List<AlarmResponse> alarms =
                alarmService.getAlarmsBySeverity(severity);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Alarms by severity retrieved.", alarms));
    }

    /**
     * GET /api/v1/alarms/{id}
     * Get a single alarm by ID.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get alarm by ID",
            description = "Retrieve a single alarm with full details including RCA result."
    )
    public ResponseEntity<ApiResponse<AlarmResponse>> getAlarmById(
            @PathVariable String id) {

        AlarmResponse response = alarmService.getAlarmById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Alarm retrieved successfully.", response));
    }

    /**
     * PATCH /api/v1/alarms/{id}/acknowledge
     * Acknowledge an alarm.
     * No request body needed — just the action.
     */
    @PatchMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NOC_MANAGER', 'NOC_ENGINEER')")
    @Operation(
            summary = "Acknowledge alarm",
            description = "Acknowledge an active alarm to indicate it is being investigated."
    )
    public ResponseEntity<ApiResponse<AlarmResponse>> acknowledgeAlarm(
            @PathVariable String id) {

        AlarmResponse response = alarmService.acknowledgeAlarm(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Alarm acknowledged.", response));
    }

    /**
     * PATCH /api/v1/alarms/{id}/resolve
     * Resolve an alarm with a resolution note.
     */
    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NOC_MANAGER', 'NOC_ENGINEER')")
    @Operation(
            summary = "Resolve alarm",
            description = "Resolve an alarm with a mandatory resolution note documenting the fix."
    )
    public ResponseEntity<ApiResponse<AlarmResponse>> resolveAlarm(
            @PathVariable String id,
            @Valid @RequestBody ResolveAlarmRequest request) {

        AlarmResponse response = alarmService.resolveAlarm(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Alarm resolved successfully.", response));
    }
}