package com.teleops.teleops_ai.device.model;

/**
 * Device Status Enum
 *
 * Represents the current operational state of a device.
 *
 * Status transitions:
 *
 *   ONLINE  ──────────► DEGRADED    (performance degradation detected)
 *   ONLINE  ──────────► OFFLINE     (device unreachable)
 *   ONLINE  ──────────► MAINTENANCE (planned maintenance window)
 *   DEGRADED ─────────► ONLINE      (issue resolved)
 *   DEGRADED ─────────► OFFLINE     (full failure)
 *   OFFLINE  ─────────► ONLINE      (device restored)
 *   MAINTENANCE ──────► ONLINE      (maintenance completed)
 *
 * Any engineer can update device status.
 * Only ADMIN/MANAGER can add or delete devices.
 */
public enum DeviceStatus {
    ONLINE,
    OFFLINE,
    DEGRADED,
    MAINTENANCE
}