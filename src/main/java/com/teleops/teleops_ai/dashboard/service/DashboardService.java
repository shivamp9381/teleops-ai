package com.teleops.teleops_ai.dashboard.service;

import com.teleops.teleops_ai.alarm.model.AlarmSeverity;
import com.teleops.teleops_ai.alarm.model.AlarmStatus;
import com.teleops.teleops_ai.alarm.repository.AlarmRepository;
import com.teleops.teleops_ai.common.util.CacheConstants;
import com.teleops.teleops_ai.dashboard.dto.DashboardStats;
import com.teleops.teleops_ai.device.model.DeviceStatus;
import com.teleops.teleops_ai.device.repository.DeviceRepository;
import com.teleops.teleops_ai.ticket.model.Ticket;
import com.teleops.teleops_ai.ticket.model.TicketStatus;
import com.teleops.teleops_ai.ticket.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dashboard Service
 *
 * Aggregates statistics from all modules for the NOC dashboard.
 *
 * This service crosses module boundaries intentionally:
 *   A dashboard, by definition, shows cross-module data.
 *   This is the one place where querying multiple repositories
 *   in a single service is justified.
 *
 * Caching strategy:
 *   The entire DashboardStats object is cached as one unit.
 *   This is more efficient than caching each count separately.
 *
 *   One Redis key → one cache check → one deserialize
 *   vs.
 *   6 Redis keys → 6 cache checks → 6 deserializes
 *
 * TTL = 60 seconds.
 *   Engineers on the dashboard get a max 60-second lag
 *   on stats. This is acceptable for NOC operations.
 *   Real-time alarms are still on the alarm board (uncached).
 *
 * @Cacheable on getDashboardStats():
 *   Spring checks Redis for "dashboard:stats" key.
 *   If found: returns cached DashboardStats, skips all DB queries.
 *   If not found: runs all 10 DB queries, caches, returns result.
 *
 * Cache invalidation:
 *   When devices or alarms change, their service methods
 *   evict DASHBOARD_STATS. This ensures stale stats
 *   are replaced at the next dashboard load.
 */
@Service
public class DashboardService {

    private static final Logger log =
            LoggerFactory.getLogger(DashboardService.class);

    private final DeviceRepository deviceRepository;
    private final AlarmRepository alarmRepository;
    private final TicketRepository ticketRepository;

    public DashboardService(DeviceRepository deviceRepository,
                            AlarmRepository alarmRepository,
                            TicketRepository ticketRepository) {
        this.deviceRepository = deviceRepository;
        this.alarmRepository = alarmRepository;
        this.ticketRepository = ticketRepository;
    }

    // ─────────────────────────────────────────────────────────
    // Get Dashboard Stats (Cached)
    // ─────────────────────────────────────────────────────────

    /**
     * Build and return the complete NOC dashboard statistics.
     *
     * Cached for 60 seconds.
     *
     * On cache miss, this method executes 10 MongoDB queries.
     * On cache hit, it returns in <5ms from Redis.
     *
     * Without caching: 10 DB queries per request
     * With caching:    10 DB queries per 60 seconds
     */
    @Cacheable(value = CacheConstants.DASHBOARD_STATS)
    public DashboardStats getDashboardStats() {
        log.debug("Cache MISS for dashboard:stats - building from MongoDB");

        DashboardStats stats = new DashboardStats();

        // ── Device Statistics ────────────────────────────────
        stats.setTotalDevices(deviceRepository.count());
        stats.setOnlineDevices(
                deviceRepository.countByStatus(DeviceStatus.ONLINE));
        stats.setOfflineDevices(
                deviceRepository.countByStatus(DeviceStatus.OFFLINE));
        stats.setDegradedDevices(
                deviceRepository.countByStatus(DeviceStatus.DEGRADED));
        stats.setMaintenanceDevices(
                deviceRepository.countByStatus(DeviceStatus.MAINTENANCE));

        // ── Alarm Statistics ─────────────────────────────────
        stats.setTotalActiveAlarms(
                alarmRepository.countByStatus(AlarmStatus.ACTIVE));
        stats.setCriticalAlarms(
                alarmRepository.countBySeverity(AlarmSeverity.CRITICAL));
        stats.setHighAlarms(
                alarmRepository.countBySeverity(AlarmSeverity.HIGH));
        stats.setMediumAlarms(
                alarmRepository.countBySeverity(AlarmSeverity.MEDIUM));
        stats.setLowAlarms(
                alarmRepository.countBySeverity(AlarmSeverity.LOW));

        // ── Ticket Statistics ────────────────────────────────
        stats.setOpenTickets(
                ticketRepository.countByStatus(TicketStatus.OPEN));
        stats.setInProgressTickets(
                ticketRepository.countByStatus(TicketStatus.IN_PROGRESS));
        stats.setResolvedTickets(
                ticketRepository.countByStatus(TicketStatus.RESOLVED));
        stats.setClosedTickets(
                ticketRepository.countByStatus(TicketStatus.CLOSED));
        stats.setTotalTickets(ticketRepository.count());

        stats.setMttrHours(calculateMttr());
        stats.setCriticalLast24h(getCriticalAlarmsLast24h());
        stats.setAvgRcaTime(getAvgRcaTime());

        log.debug("Dashboard stats computed: {} devices, {} active alarms, {} open tickets",
                stats.getTotalDevices(),
                stats.getTotalActiveAlarms(),
                stats.getOpenTickets());

        return stats;
    }

    /**
     * Calculate Mean Time To Resolve (MTTR) for tickets.
     *
     * MTTR = Average time from ticket creation to resolution.
     * Measured in hours.
     *
     * Formula:
     *   Sum of (resolvedAt - createdAt) for all resolved tickets
     *   Divided by count of resolved tickets
     */
    public double calculateMttr() {
        List<Ticket> resolved = ticketRepository
                .findByStatus(TicketStatus.RESOLVED);

        if (resolved.isEmpty()) return 0.0;

        double totalHours = resolved.stream()
                .filter(t -> t.getCreatedAt() != null &&
                        t.getResolvedAt() != null)
                .mapToLong(t -> java.time.Duration.between(
                        t.getCreatedAt(), t.getResolvedAt()).toMinutes())
                .average()
                .orElse(0.0);

        return Math.round((totalHours / 60.0) * 10.0) / 10.0; // Hours
    }

    /**
     * Count critical alarms raised in the last 24 hours.
     */
    public long getCriticalAlarmsLast24h() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return alarmRepository
                .findByRaisedAtBetween(since, LocalDateTime.now())
                .stream()
                .filter(a -> a.getSeverity() == AlarmSeverity.CRITICAL)
                .count();
    }

    /**
     * Get average AI RCA analysis time.
     * (Counted by audit log entries for AI_RCA_PERFORMED actions)
     * Placeholder — returns mock data until we track timing.
     */
    public String getAvgRcaTime() {
        return "~8 seconds";
    }
}