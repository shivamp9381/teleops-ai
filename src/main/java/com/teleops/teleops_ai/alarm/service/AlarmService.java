package com.teleops.teleops_ai.alarm.service;

import com.teleops.teleops_ai.alarm.dto.AlarmRequest;
import com.teleops.teleops_ai.alarm.dto.AlarmResponse;
import com.teleops.teleops_ai.alarm.dto.ResolveAlarmRequest;
import com.teleops.teleops_ai.alarm.model.Alarm;
import com.teleops.teleops_ai.alarm.model.AlarmSeverity;
import com.teleops.teleops_ai.alarm.model.AlarmStatus;
import com.teleops.teleops_ai.alarm.repository.AlarmRepository;
import com.teleops.teleops_ai.audit.model.AuditAction;
import com.teleops.teleops_ai.audit.service.AuditService;
import com.teleops.teleops_ai.common.exception.BadRequestException;
import com.teleops.teleops_ai.common.exception.ResourceNotFoundException;
import com.teleops.teleops_ai.device.model.Device;
import com.teleops.teleops_ai.device.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// Add to imports in AlarmService.java:
import com.teleops.teleops_ai.common.util.CacheConstants;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

/**
 * Alarm Service
 *
 * Business logic for the full alarm lifecycle.
 *
 * Cross-module dependency:
 *   AlarmService depends on DeviceRepository to:
 *     1. Validate the deviceId when raising an alarm
 *     2. Fetch device name for denormalization
 *
 *   This is a controlled cross-module dependency.
 *   Per our architecture rules:
 *     Modules can call other module REPOSITORIES in the
 *     same monolith. In a future microservice split,
 *     this would become an inter-service API call.
 *
 * Business rules enforced:
 *   1. Device must exist before raising an alarm on it
 *   2. New alarms always start as ACTIVE
 *   3. Cannot acknowledge a resolved alarm
 *   4. Cannot resolve an already-resolved alarm
 *   5. resolvedAt is set automatically on resolution
 *   6. resolvedBy is set from the current authenticated user
 */
@Service
public class AlarmService {

    private static final Logger log =
            LoggerFactory.getLogger(AlarmService.class);

    private final AlarmRepository alarmRepository;
    private final DeviceRepository deviceRepository;
    private final AuditService auditService;

    public AlarmService(AlarmRepository alarmRepository,
                        DeviceRepository deviceRepository,
                        AuditService auditService) {
        this.alarmRepository = alarmRepository;
        this.deviceRepository = deviceRepository;
        this.auditService = auditService;
    }

    // ─────────────────────────────────────────
    // Raise Alarm
    // ─────────────────────────────────────────

    /**
     * Raise a new alarm.
     *
     * Evict ALARMS_ACTIVE because a new active alarm was added.
     * Evict DASHBOARD_STATS because alarm counts changed.
     */
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.ALARMS_ACTIVE,
                    allEntries = true),
            @CacheEvict(value = CacheConstants.DASHBOARD_STATS,
                    allEntries = true)
    })
    public AlarmResponse raiseAlarm(AlarmRequest request) {

        // Step 1: Validate device exists
        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Device", "id", request.getDeviceId()));

        // Step 2: Get current user email from SecurityContext
        String currentUserEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // Step 3: Create alarm
        Alarm alarm = new Alarm(
                request.getTitle(),
                request.getDescription(),
                request.getSeverity(),
                device.getId(),
                device.getName(),    // denormalized device name
                currentUserEmail
        );

        Alarm savedAlarm = alarmRepository.save(alarm);

        auditService.log(
                currentUserEmail,
                AuditAction.ALARM_RAISED,
                "Alarm",
                savedAlarm.getId(),
                savedAlarm.getTitle(),
                "Severity: " + savedAlarm.getSeverity() +
                        " on device: " + device.getName()
        );

        log.info("Alarm raised: [{}] {} on device {} by {}",
                savedAlarm.getSeverity(),
                savedAlarm.getTitle(),
                device.getName(),
                currentUserEmail);

        return AlarmResponse.fromAlarm(savedAlarm);
    }

    // ─────────────────────────────────────────
    // Get All Alarms
    // ─────────────────────────────────────────

    /**
     * Get all alarms, most recent first.
     * Returns complete alarm list for the alarm management view.
     */
    public List<AlarmResponse> getAllAlarms() {
        return alarmRepository.findAll()
                .stream()
                .map(AlarmResponse::fromAlarm)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // Get Alarm By ID
    // ─────────────────────────────────────────

    /**
     * Get a single alarm by ID.
     * Throws 404 if not found.
     */
    public AlarmResponse getAlarmById(String id) {
        Alarm alarm = alarmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alarm", "id", id));

        return AlarmResponse.fromAlarm(alarm);
    }

    // ─────────────────────────────────────────
    // Get Active Alarms
    // ─────────────────────────────────────────

    /**
     * Get active alarms - cached with 30 second TTL.
     *
     * This is the most-read endpoint in the NOC.
     * Engineers check this constantly.
     * 30s TTL means fresh enough to be useful,
     * old enough to benefit from caching.
     */
    @Cacheable(value = CacheConstants.ALARMS_ACTIVE)
    public List<AlarmResponse> getActiveAlarms() {
        return alarmRepository
                .findByStatusOrderBySeverityAscRaisedAtDesc(AlarmStatus.ACTIVE)
                .stream()
                .map(AlarmResponse::fromAlarm)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // Get Alarms By Device
    // ─────────────────────────────────────────

    /**
     * Get all alarms raised against a specific device.
     * Used when viewing device details to show its alarm history.
     */
    public List<AlarmResponse> getAlarmsByDevice(String deviceId) {

        // Verify device exists first
        deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Device", "id", deviceId));

        return alarmRepository.findByDeviceId(deviceId)
                .stream()
                .map(AlarmResponse::fromAlarm)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // Get Alarms By Severity
    // ─────────────────────────────────────────

    /**
     * Get all alarms filtered by severity level.
     * Used for prioritized alarm views.
     */
    public List<AlarmResponse> getAlarmsBySeverity(AlarmSeverity severity) {
        return alarmRepository.findBySeverity(severity)
                .stream()
                .map(AlarmResponse::fromAlarm)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // Acknowledge Alarm
    // ─────────────────────────────────────────

    /**
     * Acknowledge alarm - evicts active alarms cache.
     * An acknowledged alarm may no longer appear in ACTIVE view.
     */
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.ALARMS_ACTIVE,
                    allEntries = true),
            @CacheEvict(value = CacheConstants.DASHBOARD_STATS,
                    allEntries = true)
    })
    public AlarmResponse acknowledgeAlarm(String id) {

        Alarm alarm = alarmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alarm", "id", id));

        // Enforce business rule
        if (alarm.getStatus() == AlarmStatus.RESOLVED) {
            throw new BadRequestException(
                    "Cannot acknowledge a resolved alarm.");
        }

        if (alarm.getStatus() == AlarmStatus.ACKNOWLEDGED) {
            throw new BadRequestException(
                    "Alarm is already acknowledged.");
        }

        alarm.setStatus(AlarmStatus.ACKNOWLEDGED);

        Alarm updatedAlarm = alarmRepository.save(alarm);

        auditService.log(
                SecurityContextHolder.getContext()
                        .getAuthentication().getName(),
                AuditAction.ALARM_ACKNOWLEDGED,
                "Alarm",
                updatedAlarm.getId(),
                updatedAlarm.getTitle(),
                "Alarm acknowledged"
        );

        log.info("Alarm acknowledged: {} [{}]",
                updatedAlarm.getTitle(), updatedAlarm.getId());

        return AlarmResponse.fromAlarm(updatedAlarm);
    }

    // ─────────────────────────────────────────
    // Resolve Alarm
    // ─────────────────────────────────────────

    /**
     * Resolve alarm - evicts active alarms cache and dashboard stats.
     * Resolved alarm leaves ACTIVE count, changes dashboard stats.
     */
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.ALARMS_ACTIVE,
                    allEntries = true),
            @CacheEvict(value = CacheConstants.DASHBOARD_STATS,
                    allEntries = true)
    })
    public AlarmResponse resolveAlarm(String id,
                                      ResolveAlarmRequest request) {

        Alarm alarm = alarmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alarm", "id", id));

        // Enforce business rule
        if (alarm.getStatus() == AlarmStatus.RESOLVED) {
            throw new BadRequestException(
                    "Alarm is already resolved.");
        }

        // Get resolver from SecurityContext
        String resolverEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        alarm.setStatus(AlarmStatus.RESOLVED);
        alarm.setResolvedBy(resolverEmail);
        alarm.setResolvedAt(LocalDateTime.now());

        // Append resolution note to description
        alarm.setDescription(alarm.getDescription()
                + "\n\n[RESOLUTION]: " + request.getResolutionNote());

        Alarm resolvedAlarm = alarmRepository.save(alarm);

        auditService.log(
                resolverEmail,
                AuditAction.ALARM_RESOLVED,
                "Alarm",
                resolvedAlarm.getId(),
                resolvedAlarm.getTitle(),
                "Resolution note added"
        );

        log.info("Alarm resolved: {} by {}",
                resolvedAlarm.getTitle(), resolverEmail);

        return AlarmResponse.fromAlarm(resolvedAlarm);
    }

    // ─────────────────────────────────────────
    // Get Recent Alarms
    // ─────────────────────────────────────────

    /**
     * Get the 10 most recently raised alarms.
     * Used for the dashboard "recent activity" widget.
     */
    public List<AlarmResponse> getRecentAlarms() {
        return alarmRepository.findTop10ByOrderByRaisedAtDesc()
                .stream()
                .map(AlarmResponse::fromAlarm)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // Dashboard Stats
    // ─────────────────────────────────────────

    /**
     * Count active alarms by severity.
     * Returns counts for dashboard statistics.
     * Used in Phase 9 (Dashboard module).
     */
    public long countActiveAlarms() {
        return alarmRepository.countByStatus(AlarmStatus.ACTIVE);
    }

    public long countAlarmsBySeverity(AlarmSeverity severity) {
        return alarmRepository.countBySeverity(severity);
    }
}