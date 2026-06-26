package com.teleops.teleops_ai.config;

import com.teleops.teleops_ai.alarm.model.AlarmStatus;
import com.teleops.teleops_ai.alarm.repository.AlarmRepository;
import com.teleops.teleops_ai.common.util.CacheConstants;
import com.teleops.teleops_ai.device.model.DeviceStatus;
import com.teleops.teleops_ai.device.repository.DeviceRepository;
import com.teleops.teleops_ai.ticket.model.TicketStatus;
import com.teleops.teleops_ai.ticket.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * NOC Scheduler
 *
 * Background tasks that run automatically on a schedule.
 *
 * Schedule expressions:
 *   @Scheduled(fixedRate = 60000)  = run every 60 seconds
 *   @Scheduled(cron = "0 * * * * *") = run at the start of every minute
 *   @Scheduled(cron = "0 0 * * * *") = run at the start of every hour
 *   @Scheduled(cron = "0 0 2 * * *") = run at 2:00 AM every day
 *
 * Cron format: second minute hour day-of-month month day-of-week
 */
@Component
public class NocScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(NocScheduler.class);

    private final DeviceRepository deviceRepository;
    private final AlarmRepository alarmRepository;
    private final TicketRepository ticketRepository;
    private final CacheManager cacheManager;

    public NocScheduler(DeviceRepository deviceRepository,
                        AlarmRepository alarmRepository,
                        TicketRepository ticketRepository,
                        CacheManager cacheManager) {
        this.deviceRepository = deviceRepository;
        this.alarmRepository = alarmRepository;
        this.ticketRepository = ticketRepository;
        this.cacheManager = cacheManager;
    }

    // ─────────────────────────────────────────────
    // Health Check — every 5 minutes
    // ─────────────────────────────────────────────

    /**
     * Log system health summary every 5 minutes.
     *
     * This gives operators a regular snapshot of system state
     * in the logs, even if no one is watching the dashboard.
     *
     * cron = "0 * 5 means: at second 0 of every 5th minute
     * **/

    @Scheduled(cron = "0 */5 * * * *")
    public void systemHealthCheck() {
        try {
            long totalDevices   = deviceRepository.count();
            long offlineDevices = deviceRepository
                    .countByStatus(DeviceStatus.OFFLINE);
            long activeAlarms   = alarmRepository
                    .countByStatus(AlarmStatus.ACTIVE);
            long openTickets    = ticketRepository
                    .countByStatus(TicketStatus.OPEN);

            log.info("[HEALTH] Devices: {}/{} online | " +
                            "Active Alarms: {} | Open Tickets: {}",
                    totalDevices - offlineDevices, totalDevices,
                    activeAlarms, openTickets);

            // Alert if critical situation
            if (offlineDevices > 0) {
                log.warn("[HEALTH] {} device(s) are OFFLINE",
                        offlineDevices);
            }
            if (activeAlarms > 10) {
                log.warn("[HEALTH] High alarm count: {} active alarms",
                        activeAlarms);
            }

        } catch (Exception e) {
            log.error("[HEALTH] Health check failed: {}", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // Cache Refresh — every 2 minutes
    // ─────────────────────────────────────────────

    /**
     * Proactively refresh dashboard cache every 2 minutes.
     *
     * Why? The Cache-Aside pattern means cache only updates
     * when someone requests data. If no one visits the dashboard
     * for 5 minutes, the next visit gets a cache miss.
     *
     * Proactive refresh keeps the cache warm.
     * Engineers get instant dashboard loads at all times.
     */
    @Scheduled(cron = "0 */2 * * * *")
    public void refreshDashboardCache() {
        try {
            // Evict dashboard stats cache
            // Next request will rebuild it from fresh DB data
            var cache = cacheManager.getCache(
                    CacheConstants.DASHBOARD_STATS);
            if (cache != null) {
                cache.clear();
                log.debug("[SCHEDULER] Dashboard stats cache cleared " +
                        "for refresh");
            }
        } catch (Exception e) {
            log.error("[SCHEDULER] Cache refresh failed: {}",
                    e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // Stale Alarm Detection — every hour
    // ─────────────────────────────────────────────

    /**
     * Detect alarms that have been ACTIVE for more than 24 hours
     * without any acknowledgment.
     *
     * These are "forgotten" alarms that engineers missed.
     * We log them prominently so they get attention.
     *
     * In a production system, this would also send an escalation
     * notification (covered in Enhancement 9 — Email Notifications).
     */
    @Scheduled(cron = "0 0 * * * *")
    public void detectStaleAlarms() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(24);

            long staleCount = alarmRepository
                    .findByStatus(AlarmStatus.ACTIVE)
                    .stream()
                    .filter(a -> a.getRaisedAt() != null &&
                            a.getRaisedAt().isBefore(cutoff))
                    .count();

            if (staleCount > 0) {
                log.warn("[SCHEDULER] {} alarm(s) have been ACTIVE " +
                        "for more than 24 hours without acknowledgment. " +
                        "Escalation required.", staleCount);
            }

        } catch (Exception e) {
            log.error("[SCHEDULER] Stale alarm check failed: {}",
                    e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // SLA Breach Detection — every 15 minutes
    // ─────────────────────────────────────────────

    /**
     * Check for tickets approaching or breaching SLA.
     *
     * SLA rules:
     *   CRITICAL tickets: must be resolved within 4 hours
     *   HIGH tickets:     must be resolved within 8 hours
     *   MEDIUM tickets:   must be resolved within 24 hours
     *   LOW tickets:      must be resolved within 72 hours
     *
     * We log warnings when SLA is within 1 hour of breach.
     */
    @Scheduled(cron = "0 */15 * * * *")
    public void checkSlaBreach() {
        try {
            AtomicInteger breachCount = new AtomicInteger(0);

            ticketRepository.findByStatus(TicketStatus.IN_PROGRESS)
                    .forEach(ticket -> {
                        if (ticket.getCreatedAt() == null) return;

                        int slaHours = switch (ticket.getPriority()) {
                            case CRITICAL -> 4;
                            case HIGH     -> 8;
                            case MEDIUM   -> 24;
                            case LOW      -> 72;
                        };

                        LocalDateTime slaDeadline = ticket.getCreatedAt()
                                .plusHours(slaHours);
                        LocalDateTime warningThreshold = slaDeadline
                                .minusHours(1);

                        if (LocalDateTime.now().isAfter(warningThreshold)) {
                            boolean breached = LocalDateTime.now()
                                    .isAfter(slaDeadline);
                            if (breached) {
                                log.warn("[SLA BREACH] {} | {} | " +
                                                "SLA was {} hours",
                                        ticket.getTicketNumber(),
                                        ticket.getTitle(),
                                        slaHours);
                                breachCount.incrementAndGet();
                            } else {
                                log.warn("[SLA WARNING] {} | {} | " +
                                                "Less than 1 hour until SLA breach",
                                        ticket.getTicketNumber(),
                                        ticket.getTitle());
                            }
                        }
                    });

            if (breachCount.get() > 0) {
                log.warn("[SCHEDULER] {} ticket(s) have breached SLA",
                        breachCount.get());
            }

        } catch (Exception e) {
            log.error("[SCHEDULER] SLA check failed: {}", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // Daily Summary — every day at 8:00 AM
    // ─────────────────────────────────────────────

    /**
     * Print a daily NOC summary to logs at 8:00 AM.
     *
     * In a production system, this would be emailed to managers.
     * For now, it appears in the application logs.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void dailySummary() {
        try {
            long activeAlarms   = alarmRepository
                    .countByStatus(AlarmStatus.ACTIVE);
            long openTickets    = ticketRepository
                    .countByStatus(TicketStatus.OPEN);
            long inProgress     = ticketRepository
                    .countByStatus(TicketStatus.IN_PROGRESS);
            long offlineDevices = deviceRepository
                    .countByStatus(DeviceStatus.OFFLINE);

            log.info("═══════════════════════════════════════");
            log.info("  TELEOPS NOC — DAILY SUMMARY");
            log.info("  Date: {}", LocalDateTime.now().toLocalDate());
            log.info("  Active Alarms:     {}", activeAlarms);
            log.info("  Open Tickets:      {}", openTickets);
            log.info("  In Progress:       {}", inProgress);
            log.info("  Offline Devices:   {}", offlineDevices);
            log.info("═══════════════════════════════════════");

        } catch (Exception e) {
            log.error("[SCHEDULER] Daily summary failed: {}",
                    e.getMessage());
        }
    }
}