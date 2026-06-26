package com.teleops.teleops_ai.alarm.repository;

import com.teleops.teleops_ai.alarm.model.Alarm;
import com.teleops.teleops_ai.alarm.model.AlarmSeverity;
import com.teleops.teleops_ai.alarm.model.AlarmStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Alarm Repository
 *
 * Spring Data MongoDB auto-implements all methods.
 *
 * Query methods generated:
 *
 *   findByStatus(ACTIVE)
 *     → db.alarms.find({ status: "ACTIVE" })
 *
 *   findBySeverity(CRITICAL)
 *     → db.alarms.find({ severity: "CRITICAL" })
 *
 *   findByDeviceId("abc123")
 *     → db.alarms.find({ deviceId: "abc123" })
 *
 *   findByStatusAndSeverity(ACTIVE, CRITICAL)
 *     → db.alarms.find({ status: "ACTIVE", severity: "CRITICAL" })
 *
 *   countByStatus(ACTIVE)
 *     → db.alarms.count({ status: "ACTIVE" })
 *
 *   findByRaisedAtBetween(start, end)
 *     → db.alarms.find({ raisedAt: { $gte: start, $lte: end } })
 *     Used for time-range reporting.
 */
@Repository
public interface AlarmRepository extends MongoRepository<Alarm, String> {

    /**
     * All alarms by status.
     * Most common query: "show all active alarms".
     */
    List<Alarm> findByStatus(AlarmStatus status);

    /**
     * All alarms by severity.
     * Used for filtering critical vs low priority alerts.
     */
    List<Alarm> findBySeverity(AlarmSeverity severity);

    /**
     * All alarms for a specific device.
     * Used when viewing a device's alarm history.
     */
    List<Alarm> findByDeviceId(String deviceId);

    /**
     * Filter by both status and severity.
     * Example: "all ACTIVE CRITICAL alarms".
     */
    List<Alarm> findByStatusAndSeverity(AlarmStatus status,
                                        AlarmSeverity severity);

    /**
     * All alarms for a device filtered by status.
     * Used in ticket creation: "active alarms for this device".
     */
    List<Alarm> findByDeviceIdAndStatus(String deviceId,
                                        AlarmStatus status);

    /**
     * Count alarms by status.
     * Used for dashboard statistics.
     */
    long countByStatus(AlarmStatus status);

    /**
     * Count alarms by severity.
     * Used for dashboard statistics.
     */
    long countBySeverity(AlarmSeverity severity);

    /**
     * Time-range query for alarms.
     * Used for reporting: "alarms raised last 7 days".
     */
    List<Alarm> findByRaisedAtBetween(LocalDateTime start,
                                      LocalDateTime end);

    /**
     * Most recent alarms first, limited to N results.
     * Used for dashboard "recent alarms" widget.
     */
    List<Alarm> findTop10ByOrderByRaisedAtDesc();

    /**
     * Find alarms by status ordered by severity and time.
     * Used for alarm queue: most critical first.
     */
    List<Alarm> findByStatusOrderBySeverityAscRaisedAtDesc(
            AlarmStatus status);
}