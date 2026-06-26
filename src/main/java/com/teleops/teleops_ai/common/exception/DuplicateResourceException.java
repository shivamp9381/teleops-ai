package com.teleops.teleops_ai.common.exception;

/**
 * Thrown when trying to create a resource that already exists.
 *
 * Maps to HTTP 409 Conflict.
 *
 * Examples:
 *   - Registering with an email that already exists
 *   - Creating a device with an IP address already in use
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}