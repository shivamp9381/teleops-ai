package com.teleops.teleops_ai.alarm.model;

/**
 * Alarm Status Enum
 *
 * Represents the current state of an alarm in its lifecycle.
 *
 * ACTIVE       = Alarm is firing, no action taken yet
 * ACKNOWLEDGED = Engineer has seen it and is working on it
 * RESOLVED     = Issue has been fixed, alarm is closed
 *
 * Valid transitions:
 *   ACTIVE → ACKNOWLEDGED
 *   ACTIVE → RESOLVED
 *   ACKNOWLEDGED → RESOLVED
 *
 * Invalid transitions (enforced in service):
 *   RESOLVED → anything  (resolved alarms cannot be reopened)
 *   ACKNOWLEDGED → ACTIVE (cannot go backwards)
 */
public enum AlarmStatus {
    ACTIVE,
    ACKNOWLEDGED,
    RESOLVED
}