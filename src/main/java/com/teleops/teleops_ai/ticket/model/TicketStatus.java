package com.teleops.teleops_ai.ticket.model;

/**
 * Ticket Status Enum
 *
 * Represents the current state of an incident ticket.
 *
 * Valid transitions:
 *   OPEN        → IN_PROGRESS  (manager assigns to engineer)
 *   OPEN        → RESOLVED     (direct resolution, skip assignment)
 *   IN_PROGRESS → RESOLVED     (engineer resolves)
 *   RESOLVED    → CLOSED       (manager closes after review)
 *
 * Invalid transitions (enforced in service):
 *   CLOSED → anything           (closed tickets are permanent)
 *   RESOLVED → IN_PROGRESS      (cannot go backwards)
 *   RESOLVED → OPEN             (cannot reopen)
 */
public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED
}