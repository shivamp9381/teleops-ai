package com.teleops.teleops_ai.device.model;

/**
 * Device Type Enum
 *
 * Represents the physical type of a network device.
 *
 * This enum is stored as a String in MongoDB (not ordinal).
 * String storage is safer because:
 *   - Adding new types never corrupts existing data
 *   - Reordering enum values does not break anything
 *   - Readable in the database without decoding
 *
 * Types explained:
 *   4G_TOWER      = LTE base transceiver station
 *   5G_TOWER      = NR (New Radio) gNodeB
 *   ROUTER        = IP routing device
 *   FIREWALL      = Network security device
 *   SWITCH        = Layer 2/3 network switch
 *   BASE_STATION  = Generic radio base station (2G/3G)
 */
public enum DeviceType {
    TOWER_4G,
    TOWER_5G,
    ROUTER,
    FIREWALL,
    SWITCH,
    BASE_STATION
}