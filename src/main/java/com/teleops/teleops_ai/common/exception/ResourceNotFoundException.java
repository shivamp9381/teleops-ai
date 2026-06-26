package com.teleops.teleops_ai.common.exception;

/**
 * Thrown when a requested resource does not exist in the database.
 *
 * Maps to HTTP 404 Not Found.
 *
 * Examples:
 *   - GET /devices/{id} → device with that ID does not exist
 *   - GET /alarms/{id}  → alarm not found
 *   - PATCH /tickets/{id}/assign → ticket not found
 *
 * We extend RuntimeException (not Exception) because:
 *   - We do not want to force every caller to use try/catch
 *   - Spring handles unchecked exceptions cleanly
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * @param resourceName The type of resource (e.g., "Device", "Alarm")
     * @param fieldName    The field used to search (e.g., "id", "email")
     * @param fieldValue   The value that was searched for
     *
     * Produces message: "Device not found with id : 'abc123'"
     */
    public ResourceNotFoundException(String resourceName,
                                     String fieldName,
                                     Object fieldValue) {
        super(String.format("%s not found with %s : '%s'",
                resourceName, fieldName, fieldValue));
    }

    /**
     * Simple constructor for when a custom message is cleaner.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}