package com.teleops.teleops_ai.common.util;

/**
 * Cache Key Constants
 *
 * Centralizing cache key names prevents typos and makes
 * cache management easier.
 *
 * If you rename a cache key, you change it in ONE place.
 * Without this, you would have string literals scattered
 * across multiple service files.
 *
 * Convention:
 *   module:entity          = collection-level cache
 *   module:entity:{id}     = individual entity cache
 *   module:stats           = aggregated statistics
 */
public final class CacheConstants {

    // Private constructor prevents instantiation
    private CacheConstants() {}

    // ─────────────────────────────────────────────
    // Cache Names (used in @Cacheable value field)
    // ─────────────────────────────────────────────

    /**
     * All devices list.
     * Invalidated when any device is created, updated, or deleted.
     */
    public static final String DEVICES_ALL = "devices:all";

    /**
     * Individual device by ID.
     * Key pattern: devices:{id}
     * Invalidated when that specific device changes.
     */
    public static final String DEVICES = "devices";

    /**
     * Active alarms list (status = ACTIVE).
     * Short TTL because alarm state changes frequently.
     */
    public static final String ALARMS_ACTIVE = "alarms:active";

    /**
     * Dashboard aggregated statistics.
     * Built from multiple MongoDB queries.
     * Cached as a single object to avoid repeated aggregation.
     */
    public static final String DASHBOARD_STATS = "dashboard:stats";
}