package com.teleops.teleops_ai.auth.model;

/**
 * User Roles for Role-Based Access Control (RBAC)
 *
 * Spring Security uses the prefix "ROLE_" internally.
 * When we annotate with @PreAuthorize("hasRole('NOC_MANAGER')")
 * Spring checks for the authority "ROLE_NOC_MANAGER".
 *
 * We store the plain enum name in MongoDB (without ROLE_ prefix)
 * and let Spring Security handle the prefix mapping.
 *
 * Role Hierarchy (highest to lowest):
 *   SUPER_ADMIN  → Full system access
 *   NOC_MANAGER  → Operations management
 *   NOC_ENGINEER → Day-to-day operations
 *   READ_ONLY    → View only
 */
public enum Role {
    SUPER_ADMIN,
    NOC_MANAGER,
    NOC_ENGINEER,
    READ_ONLY
}