package com.teleops.teleops_ai.auth.controller;

import com.teleops.teleops_ai.auth.dto.AuthResponse;
import com.teleops.teleops_ai.auth.dto.LoginRequest;
import com.teleops.teleops_ai.auth.dto.RegisterRequest;
import com.teleops.teleops_ai.auth.dto.UserResponse;
import com.teleops.teleops_ai.auth.service.AuthService;
import com.teleops.teleops_ai.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 *
 * Handles all HTTP requests for the auth module.
 *
 * Controller responsibilities (ONLY these):
 *   1. Receive and validate HTTP requests
 *   2. Call the service with clean input
 *   3. Wrap the response in ApiResponse
 *   4. Return the correct HTTP status code
 *
 * NO business logic in the controller.
 * NO direct repository calls in the controller.
 *
 * @RestController = @Controller + @ResponseBody
 *   Every method returns JSON automatically.
 *
 * @RequestMapping sets the base path for all methods.
 *
 * @Valid triggers Bean Validation on the request body.
 *   If validation fails, MethodArgumentNotValidException is thrown
 *   and caught by our GlobalExceptionHandler.
 *
 * @AuthenticationPrincipal injects the currently authenticated
 *   UserDetails directly from the SecurityContext.
 *   No need to manually parse the JWT in the controller.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Register, login, and token management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/v1/auth/register
     *
     * Register a new user account.
     * Public endpoint — no JWT required.
     *
     * Returns 201 Created on success.
     * Returns 409 Conflict if email already exists.
     * Returns 400 Bad Request if validation fails.
     */
    @PostMapping("/register")
    @Operation(
            summary = "Register new user",
            description = "Create a new user account. Returns JWT tokens on success."
    )
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "User registered successfully.", response));
    }

    /**
     * POST /api/v1/auth/login
     *
     * Authenticate user with email and password.
     * Public endpoint — no JWT required.
     *
     * Returns 200 OK with JWT tokens on success.
     * Returns 401 Unauthorized if credentials are wrong.
     */
    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticate with email and password. Returns JWT access and refresh tokens."
    )
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful.", response));
    }

    /**
     * POST /api/v1/auth/refresh
     *
     * Get a new access token using a refresh token.
     * Public endpoint — no (valid) access token needed.
     * Client sends: { "refreshToken": "..." }
     *
     * Returns 200 OK with new access token.
     * Returns 400 Bad Request if refresh token is invalid/expired.
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Use a valid refresh token to get a new access token."
    )
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestBody RefreshTokenRequest request) {

        AuthResponse response = authService.refreshToken(
                request.getRefreshToken());

        return ResponseEntity.ok(
                ApiResponse.success("Token refreshed successfully.", response));
    }

    /**
     * GET /api/v1/auth/me
     *
     * Get the currently authenticated user's profile.
     * Protected endpoint — requires valid JWT.
     *
     * @AuthenticationPrincipal injects the UserDetails
     * from the SecurityContext set by JwtAuthFilter.
     */
    @GetMapping("/me")
    @Operation(
            summary = "Get current user profile",
            description = "Returns the profile of the currently authenticated user."
    )
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {

        UserResponse response = authService.getCurrentUser(
                userDetails.getUsername());

        return ResponseEntity.ok(
                ApiResponse.success("User profile retrieved.", response));
    }

    /**
     * Inner class for the refresh token request body.
     *
     * We define it here since it is only used in this controller.
     * No need to create a separate file for a two-field class.
     */
    public static class RefreshTokenRequest {

        private String refreshToken;

        public RefreshTokenRequest() {
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}