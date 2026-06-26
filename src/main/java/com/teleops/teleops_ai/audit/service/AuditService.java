package com.teleops.teleops_ai.audit.service;

import com.teleops.teleops_ai.audit.model.AuditAction;
import com.teleops.teleops_ai.audit.model.AuditLog;
import com.teleops.teleops_ai.audit.repository.AuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Audit Service
 *
 * Key design decisions:
 *
 * 1. @Async on log() method:
 *    Audit logging must NEVER slow down the main request.
 *    If MongoDB is slow, the user should not feel it.
 *    We log asynchronously — fire and forget.
 *
 * 2. Never throw exceptions:
 *    Audit logging failure must not break the business operation.
 *    We catch all exceptions and log them instead.
 *
 * 3. Called from service layer, not controller:
 *    Services know the business context.
 *    Controllers only know HTTP.
 */
@Service
public class AuditService {

    private static final Logger log =
            LoggerFactory.getLogger(AuditService.class);

    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    // ─────────────────────────────────────────────
    // Log an action (async, never throws)
    // ─────────────────────────────────────────────

    /**
     * Record an audit event asynchronously.
     *
     * @Async means this runs on a separate thread pool thread.
     * The calling thread does not wait for this to complete.
     *
     * If this fails, the error is logged but not propagated.
     */
    @Async
    public void log(String userEmail, AuditAction action,
                    String resourceType, String resourceId,
                    String resourceName, String details) {
        try {
            AuditLog entry = AuditLog.of(
                    userEmail, action, resourceType,
                    resourceId, resourceName, details);
            auditRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to write audit log: action={}, user={}, error={}",
                    action, userEmail, e.getMessage());
        }
    }

    /**
     * Log with IP address (for auth events).
     */
    @Async
    public void logWithIp(String userEmail, AuditAction action,
                          String resourceType, String resourceId,
                          String resourceName, String details,
                          String ipAddress) {
        try {
            AuditLog entry = AuditLog.of(
                    userEmail, action, resourceType,
                    resourceId, resourceName, details);
            entry.setIpAddress(ipAddress);
            auditRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to write audit log with IP: {}",
                    e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // Query methods
    // ─────────────────────────────────────────────

    public Page<AuditLog> getAllLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditRepository.findAllByOrderByTimestampDesc(pageable);
    }

    public Page<AuditLog> getLogsByUser(String userEmail,
                                        int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditRepository
                .findByUserEmailOrderByTimestampDesc(userEmail, pageable);
    }

    public Page<AuditLog> getLogsByResource(String resourceId,
                                            int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditRepository
                .findByResourceIdOrderByTimestampDesc(resourceId, pageable);
    }

    public List<AuditLog> getLogsByDateRange(LocalDateTime start,
                                             LocalDateTime end) {
        return auditRepository
                .findByTimestampBetweenOrderByTimestampDesc(start, end);
    }
}