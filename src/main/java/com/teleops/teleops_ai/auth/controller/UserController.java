package com.teleops.teleops_ai.auth.controller;

import com.teleops.teleops_ai.auth.dto.*;
import com.teleops.teleops_ai.auth.model.Role;
import com.teleops.teleops_ai.auth.service.UserService;
import com.teleops.teleops_ai.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User Management Controller
 *
 * REST endpoints for user profile and management operations.
 *
 * Base path: /api/v1/users
 *
 * ─────────────────────────────────────────────────────────────
 * Endpoint Overview:
 * ─────────────────────────────────────────────────────────────
 *
 * Own profile (all authenticated users):
 *   GET    /users/me/profile        = view own full profile
 *   PATCH  /users/me/profile        = update own name
 *   PATCH  /users/me/password       = change own password
 *
 * Team management (manager + admin):
 *   GET    /users/team              = get subordinates
 *   GET    /users/engineers/active  = get active engineers (for assignment)
 *
 * User lookup (admin full, manager restricted):
 *   GET    /users                   = all users (SUPER_ADMIN only)
 *   GET    /users/stats             = user statistics (SUPER_ADMIN only)
 *   GET    /users/role/{role}       = users by role (admin + manager)
 *   GET    /users/{id}/profile      = any user's profile
 *
 * Admin actions (SUPER_ADMIN only):
 *   PATCH  /users/{id}/activate     = activate user
 *   PATCH  /users/{id}/deactivate   = deactivate user
 *   PATCH  /users/{id}/role         = change user role
 *
 * ─────────────────────────────────────────────────────────────
 * URL ordering note:
 *   /users/me/profile, /users/team, /users/stats,
 *   /users/engineers/active, /users/role/{role}
 *   MUST be declared BEFORE /users/{id}/profile
 *   to prevent "me", "team", etc. being treated as {id}.
 * ─────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management",
        description = "User profiles, team management, and admin operations")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ─────────────────────────────────────────────────────────────
    // Own Profile Endpoints (all authenticated users)
    // ─────────────────────────────────────────────────────────────

    /**
     * GET /api/v1/users/me/profile
     *
     * Get the currently authenticated user's full profile
     * including activity statistics.
     *
     * Accessible by ALL authenticated users.
     */
    @GetMapping("/me/profile")
    @Operation(
            summary = "Get my full profile",
            description = "Get the authenticated user's full profile including " +
                    "activity statistics (alarms raised, tickets created/resolved)."
    )
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {

        UserProfileResponse profile = userService.getMyProfile();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Profile retrieved successfully.", profile));
    }

    /**
     * PATCH /api/v1/users/me/profile
     *
     * Update the current user's own profile (name only).
     * All authenticated users can update their own name.
     */
    @PatchMapping("/me/profile")
    @Operation(
            summary = "Update my profile",
            description = "Update your own name. Email and role cannot be changed here."
    )
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {

        UserResponse response = userService.updateMyProfile(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Profile updated successfully.", response));
    }

    /**
     * PATCH /api/v1/users/me/password
     *
     * Change the current user's password.
     * Requires current password verification.
     */
    @PatchMapping("/me/password")
    @Operation(
            summary = "Change my password",
            description = "Change your own password. " +
                    "Current password must be provided for verification."
    )
    public ResponseEntity<ApiResponse<Void>> changeMyPassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changeMyPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success("Password changed successfully."));
    }

    // ─────────────────────────────────────────────────────────────
    // Team Endpoints (Manager + Super Admin)
    // ─────────────────────────────────────────────────────────────

    /**
     * GET /api/v1/users/team
     *
     * Get the subordinate team of the current user.
     *
     * SUPER_ADMIN: returns all managers, engineers, and read-only users.
     * NOC_MANAGER: returns all NOC_ENGINEER users.
     * Others: returns empty list.
     */
    @GetMapping("/team")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NOC_MANAGER')")
    @Operation(
            summary = "Get my team",
            description = "Get subordinate users. " +
                    "Super Admin sees all staff. " +
                    "Manager sees all engineers."
    )
    public ResponseEntity<ApiResponse<List<UserResponse>>> getMyTeam() {

        List<UserResponse> team = userService.getMyTeam();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Team retrieved successfully.", team));
    }

    /**
     * GET /api/v1/users/engineers/active
     *
     * Get all active engineers available for ticket assignment.
     *
     * Used by the ticket assignment feature.
     * Managers need this list to select who to assign a ticket to.
     *
     * MUST be declared before /{id}/profile
     */
    @GetMapping("/engineers/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NOC_MANAGER')")
    @Operation(
            summary = "Get active engineers",
            description = "Get all active NOC Engineers available for ticket assignment."
    )
    public ResponseEntity<ApiResponse<List<UserResponse>>> getActiveEngineers() {

        List<UserResponse> engineers = userService.getActiveEngineers();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Active engineers retrieved.", engineers));
    }

    // ─────────────────────────────────────────────────────────────
    // Admin Endpoints (Super Admin Only)
    // ─────────────────────────────────────────────────────────────

    /**
     * GET /api/v1/users
     *
     * Get all users in the system.
     * SUPER_ADMIN only.
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Get all users",
            description = "Get complete list of all users. Super Admin only."
    )
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {

        List<UserResponse> users = userService.getAllUsers();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "All users retrieved successfully.", users));
    }

    /**
     * GET /api/v1/users/stats
     *
     * Get user statistics.
     * SUPER_ADMIN only.
     *
     * MUST be declared before /{id}/profile
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Get user statistics",
            description = "Get counts of users by role and active status. Super Admin only."
    )
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats() {

        UserStatsResponse stats = userService.getUserStats();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User statistics retrieved.", stats));
    }

    /**
     * GET /api/v1/users/role/{role}
     *
     * Get all users with a specific role.
     *
     * SUPER_ADMIN: can query any role.
     * NOC_MANAGER: can only query NOC_ENGINEER.
     *
     * MUST be declared before /{id}/profile
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'NOC_MANAGER')")
    @Operation(
            summary = "Get users by role",
            description = "Get all users with a specific role. " +
                    "Managers can only query NOC_ENGINEER."
    )
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(
            @PathVariable Role role,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<UserResponse> users = userService.getUsersByRole(
                role, userDetails.getUsername());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Users by role retrieved.", users));
    }

    /**
     * GET /api/v1/users/{id}/profile
     *
     * Get a specific user's full profile.
     *
     * Access rules enforced in service:
     *   SUPER_ADMIN: can view any profile
     *   NOC_MANAGER: can view own + engineer profiles
     *   Others: own profile only
     */
    @GetMapping("/{id}/profile")
    @Operation(
            summary = "Get user profile by ID",
            description = "Get a user's full profile. " +
                    "Access rules: Super Admin sees all. " +
                    "Manager sees engineers. " +
                    "Others see own profile only."
    )
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserProfileResponse profile = userService.getUserProfile(
                id, userDetails.getUsername());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User profile retrieved.", profile));
    }

    /**
     * PATCH /api/v1/users/{id}/activate
     *
     * Activate a user account.
     * SUPER_ADMIN only.
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Activate user",
            description = "Activate a deactivated user account. Super Admin only."
    )
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(
            @PathVariable String id) {

        UserResponse response = userService.activateUser(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User account activated successfully.", response));
    }

    /**
     * PATCH /api/v1/users/{id}/deactivate
     *
     * Deactivate a user account (soft disable).
     * SUPER_ADMIN only.
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Deactivate user",
            description = "Deactivate a user account. " +
                    "User cannot log in but data is preserved. Super Admin only."
    )
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(
            @PathVariable String id) {

        UserResponse response = userService.deactivateUser(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User account deactivated successfully.", response));
    }

    /**
     * PATCH /api/v1/users/{id}/role
     *
     * Change a user's role.
     * SUPER_ADMIN only.
     */
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Change user role",
            description = "Change a user's role. Cannot promote to SUPER_ADMIN. Super Admin only."
    )
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRoleRequest request) {

        UserResponse response = userService.updateUserRole(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User role updated successfully.", response));
    }
}