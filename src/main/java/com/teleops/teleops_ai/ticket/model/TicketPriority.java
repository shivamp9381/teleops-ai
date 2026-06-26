package com.teleops.teleops_ai.ticket.model;

/**
 * Ticket Priority Enum
 *
 * Represents the urgency of resolving the ticket.
 *
 * Priority is set when creating the ticket and can be updated.
 * It should align with the alarm severity that triggered it:
 *
 *   Alarm CRITICAL  → Ticket CRITICAL
 *   Alarm HIGH      → Ticket HIGH
 *   Alarm MEDIUM    → Ticket MEDIUM
 *   Alarm LOW       → Ticket LOW
 *
 * But tickets can exist without an alarm (standalone),
 * in which case the engineer sets the priority manually.
 */
public enum TicketPriority {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}