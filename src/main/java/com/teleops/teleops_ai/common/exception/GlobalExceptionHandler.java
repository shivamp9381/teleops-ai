package com.teleops.teleops_ai.common.exception;

import com.teleops.teleops_ai.common.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * This class intercepts exceptions thrown anywhere in the
 * application and converts them into proper HTTP responses.
 *
 * The order of handlers matters:
 *   More specific exceptions are handled first.
 *   Generic Exception is the final catch-all.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles: ResourceNotFoundException
     * HTTP Status: 404 Not Found
     *
     * Triggered when: Device/Alarm/Ticket not found by ID
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException ex) {

        log.warn("Resource not found: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handles: BadRequestException
     * HTTP Status: 400 Bad Request
     *
     * Triggered when: Invalid business operation attempted
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(
            BadRequestException ex) {

        log.warn("Bad request: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handles: DuplicateResourceException
     * HTTP Status: 409 Conflict
     *
     * Triggered when: Duplicate email, duplicate device IP
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicate(
            DuplicateResourceException ex) {

        log.warn("Duplicate resource: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handles: UnauthorizedException
     * HTTP Status: 403 Forbidden
     *
     * Triggered when: User lacks permission for the operation
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
            UnauthorizedException ex) {

        log.warn("Unauthorized access: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Handles: Spring Security AccessDeniedException
     * HTTP Status: 403 Forbidden
     *
     * Triggered when: @PreAuthorize annotation blocks access
     * This is a Spring Security exception, not our custom one.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex) {

        log.warn("Access denied: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(
                        "Access denied. You do not have permission to perform this action."));
    }

    /**
     * Handles: BadCredentialsException
     * HTTP Status: 401 Unauthorized
     *
     * Triggered when: Wrong email or password during login
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
            BadCredentialsException ex) {

        log.warn("Bad credentials attempt");

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid email or password."));
    }

    /**
     * Handles: MethodArgumentNotValidException
     * HTTP Status: 400 Bad Request
     *
     * Triggered when: @Valid fails on a request body.
     * Example: email field is blank, password too short.
     *
     * Returns a map of fieldName -> errorMessage so the
     * frontend can highlight the specific invalid field.
     *
     * Example response data:
     * {
     *   "email": "must not be blank",
     *   "password": "size must be between 8 and 100"
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });

        log.warn("Validation failed: {}", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.success("Validation failed", errors));
    }

    /**
     * Handles: All other unhandled exceptions
     * HTTP Status: 500 Internal Server Error
     *
     * This is the safety net. We log the full stack trace
     * internally but return a generic message to the client.
     *
     * IMPORTANT:
     * We NEVER expose stack traces or internal error details
     * to clients in production. That is a security risk.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex) {

        // Log the full exception internally for debugging
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "An unexpected error occurred. Please try again later."));
    }
}