package com.teleops.teleops_ai.alarm.model;

/**
 * Alarm Severity Enum
 *
 * Represents the urgency and impact level of an alarm.
 *
 * Stored as String in MongoDB for readability.
 * Ordered from highest to lowest severity.
 *
 * These values align with ITU-T M.3100 telecom standards
 * for fault management severity levels.
 */
public enum AlarmSeverity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}