package com.teleops.teleops_ai.device.service;

import com.teleops.teleops_ai.alarm.model.AlarmSeverity;
import com.teleops.teleops_ai.alarm.model.AlarmStatus;
import com.teleops.teleops_ai.alarm.repository.AlarmRepository;
import com.teleops.teleops_ai.device.dto.DeviceHealthScore;
import com.teleops.teleops_ai.device.model.Device;
import com.teleops.teleops_ai.device.model.DeviceStatus;
import com.teleops.teleops_ai.device.repository.DeviceRepository;
import com.teleops.teleops_ai.ticket.model.TicketStatus;
import com.teleops.teleops_ai.ticket.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Device Health Score Service
 *
 * Calculates a 0-100 health score for each device based on:
 *
 *   Scoring factors:
 *     Device status           (0-40 points)
 *       ONLINE:      40 points
 *       DEGRADED:    20 points
 *       MAINTENANCE: 10 points
 *       OFFLINE:      0 points
 *
 *     Recent alarms in 24h   (0-40 points)
 *       0 alarms:       40 points
 *       1-2 alarms:     30 points
 *       3-5 alarms:     20 points
 *       5+ alarms:       0 points
 *       Critical alarm: -20 penalty
 *
 *     Open tickets           (0-20 points)
 *       0 tickets:       20 points
 *       1 ticket:        15 points
 *       2+ tickets:       5 points
 *
 * Score interpretation:
 *   90-100  EXCELLENT — Device is healthy
 *   70-89   GOOD      — Minor issues
 *   50-69   FAIR      — Needs attention
 *   30-49   POOR      — Serious issues
 *   0-29    CRITICAL  — Immediate action required
 */
@Service
public class DeviceHealthService {

    private final DeviceRepository deviceRepository;
    private final AlarmRepository alarmRepository;
    private final TicketRepository ticketRepository;

    public DeviceHealthService(DeviceRepository deviceRepository,
                               AlarmRepository alarmRepository,
                               TicketRepository ticketRepository) {
        this.deviceRepository = deviceRepository;
        this.alarmRepository = alarmRepository;
        this.ticketRepository = ticketRepository;
    }

    public DeviceHealthScore calculateHealth(String deviceId) {

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException(
                        "Device not found: " + deviceId));

        int score = 0;
        StringBuilder factors = new StringBuilder();

        // Factor 1: Device status (0-40 points)
        int statusScore = switch (device.getStatus()) {
            case ONLINE      -> 40;
            case DEGRADED    -> 20;
            case MAINTENANCE -> 10;
            case OFFLINE     ->  0;
        };
        score += statusScore;
        factors.append("Status (").append(device.getStatus())
                .append("): +").append(statusScore).append(". ");

        // Factor 2: Recent alarms in last 24 hours (0-40 points)
        LocalDateTime last24h = LocalDateTime.now().minusHours(24);
        List<com.teleops.teleops_ai.alarm.model.Alarm> recentAlarms = alarmRepository
                .findByDeviceId(deviceId)
                .stream()
                .filter(a -> a.getRaisedAt() != null &&
                        a.getRaisedAt().isAfter(last24h))
                .collect(Collectors.toList());

        int alarmCount = recentAlarms.size();
        int alarmScore;
        if      (alarmCount == 0)      alarmScore = 40;
        else if (alarmCount <= 2)      alarmScore = 30;
        else if (alarmCount <= 5)      alarmScore = 20;
        else                           alarmScore = 0;

        // Critical alarm penalty
        boolean hasCritical = recentAlarms.stream()
                .anyMatch(a -> a.getSeverity() == AlarmSeverity.CRITICAL
                        && a.getStatus() == AlarmStatus.ACTIVE);
        if (hasCritical) {
            alarmScore = Math.max(0, alarmScore - 20);
            factors.append("Critical alarm penalty: -20. ");
        }

        score += alarmScore;
        factors.append("Alarms (").append(alarmCount).append(" in 24h): +")
                .append(alarmScore).append(". ");

        // Factor 3: Open tickets (0-20 points)
        long openTicketCount = ticketRepository.findByDeviceId(deviceId)
                .stream()
                .filter(t -> t.getStatus() == TicketStatus.OPEN ||
                        t.getStatus() == TicketStatus.IN_PROGRESS)
                .count();

        int ticketScore;
        if      (openTicketCount == 0) ticketScore = 20;
        else if (openTicketCount == 1) ticketScore = 15;
        else                           ticketScore = 5;

        score += ticketScore;
        factors.append("Open Tickets (").append(openTicketCount).append("): +")
                .append(ticketScore).append(".");

        // Determine grade
        String grade;
        String status;
        if      (score >= 90) { grade = "A"; status = "EXCELLENT"; }
        else if (score >= 70) { grade = "B"; status = "GOOD"; }
        else if (score >= 50) { grade = "C"; status = "FAIR"; }
        else if (score >= 30) { grade = "D"; status = "POOR"; }
        else                  { grade = "F"; status = "CRITICAL"; }

        return new DeviceHealthScore(
                deviceId,
                device.getName(),
                score,
                grade,
                status,
                alarmCount,
                (int) openTicketCount,
                factors.toString()
        );
    }

    public List<DeviceHealthScore> calculateAllHealthScores() {
        return deviceRepository.findAll()
                .stream()
                .map(d -> calculateHealth(d.getId()))
                .sorted((a, b) -> Integer.compare(a.getScore(), b.getScore()))
                .collect(Collectors.toList());
    }
}