package com.teleops.teleops_ai.common.exception;

/**
 * Thrown when a user tries to perform an action they
 * do not have permission to perform.
 *
 * Maps to HTTP 403 Forbidden.
 *
 * Note the difference:
 *   401 Unauthorized = not authenticated (no valid JWT)
 *   403 Forbidden    = authenticated but not authorized (wrong role)
 *
 * Spring Security handles 401 automatically via the JWT filter.
 * We throw this exception for business-logic-level 403 scenarios.
 *
 * Examples:
 *   - READ_ONLY user tries to create a ticket
 *   - NOC_ENGINEER tries to delete a device
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}