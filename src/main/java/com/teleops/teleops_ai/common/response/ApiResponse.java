package com.teleops.teleops_ai.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * Standard API Response Wrapper
 *
 * Every endpoint in this application returns this class.
 * This gives the frontend a consistent contract to code against.
 *
 * Generic type T allows us to wrap any data type:
 *   ApiResponse<DeviceResponse>
 *   ApiResponse<List<AlarmResponse>>
 *   ApiResponse<Void>  (for operations with no return data)
 *
 * @JsonInclude(NON_NULL) - fields with null values are NOT
 * included in the JSON output. Keeps responses clean.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    // Private constructor - use static factory methods below
    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Use this when operation succeeds AND returns data.
     * Example: GET /devices/{id} → returns device data
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Use this when operation succeeds but returns no data.
     * Example: DELETE /devices/{id} → just confirm success
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    /**
     * Use this when operation fails.
     * Example: Device not found, validation error
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    // Getters - no setters, this object is effectively immutable
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}