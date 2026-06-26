package com.teleops.teleops_ai.common.exception;

/**
 * Thrown when the client sends an invalid request.
 *
 * Maps to HTTP 400 Bad Request.
 *
 * Examples:
 *   - Trying to resolve an already-resolved alarm
 *   - Assigning a ticket to a user who is not an engineer
 *   - Creating a device with a duplicate IP address
 *   - Invalid status transition (e.g., CLOSED → OPEN)
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}