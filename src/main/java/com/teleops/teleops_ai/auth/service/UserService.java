//package com.teleops.teleops_ai.auth.service;
//
//import com.teleops.teleops_ai.alarm.repository.AlarmRepository;
//import com.teleops.teleops_ai.audit.model.AuditAction;
//import com.teleops.teleops_ai.audit.service.AuditService;
//import com.teleops.teleops_ai.auth.dto.*;
//import com.teleops.teleops_ai.auth.model.Role;
//import com.teleops.teleops_ai.auth.model.User;
//import com.teleops.teleops_ai.auth.repository.UserRepository;
//import com.teleops.teleops_ai.common.exception.BadRequestException;
//import com.teleops.teleops_ai.common.exception.ResourceNotFoundException;
//import com.teleops.teleops_ai.common.exception.UnauthorizedException;
//import com.teleops.teleops_ai.ticket.model.TicketStatus;
//import com.teleops.teleops_ai.ticket.repository.TicketRepository;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
///**
// * User Management Service
// *
// * Handles all user management operations:
// *   1. View all users (Super Admin only)
// *   2. View subordinates (Manager views engineers, Admin views all)
// *   3. View any user profile with activity stats
// *   4. Update own profile (name)
// *   5. Change own password
// *   6. Activate/deactivate users (Super Admin only)
// *   7. Change user role (Super Admin only)
// *
// * Access control rules:
// *   SUPER_ADMIN:
// *     - Can view ALL users
// *     - Can view ANY user's profile
// *     - Can activate/deactivate any user
// *     - Can change any user's role
// *     - Can view all managers and engineers
// *
// *   NOC_MANAGER:
// *     - Can view all NOC_ENGINEER profiles
// *     - Can view their own profile
// *     - Cannot view other managers' profiles
// *     - Cannot activate/deactivate users
// *     - Cannot change roles
// *
// *   NOC_ENGINEER / READ_ONLY:
// *     - Can only view their own profile
// *     - Can update their own name
// *     - Can change their own password
// *
// * Why managers can view engineer profiles?
// *   Managers need to know their team:
// *   - Who is available (active users)
// *   - Who has how many tickets assigned
// *   - Who resolved the most issues (performance)
// *   This data helps managers assign tickets fairly.
// */
//@Service
//public class UserService {
//
//    private static final Logger log =
//            LoggerFactory.getLogger(UserService.class);
//
//    private final UserRepository userRepository;
//    private final AlarmRepository alarmRepository;
//    private final TicketRepository ticketRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final AuditService auditService;
//
//    public UserService(UserRepository userRepository,
//                       AlarmRepository alarmRepository,
//                       TicketRepository ticketRepository,
//                       PasswordEncoder passwordEncoder,
//                       AuditService auditService) {
//        this.userRepository = userRepository;
//        this.alarmRepository = alarmRepository;
//        this.ticketRepository = ticketRepository;
//        this.passwordEncoder = passwordEncoder;
//        this.auditService = auditService;
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    // Get Current User's Own Profile
//    // ─────────────────────────────────────────────────────────────
//
//    /**
//     * Get the currently authenticated user's full profile
//     * including activity statistics.
//     *
//     * Accessible by ALL authenticated users for their own profile.
//     */
//    public UserProfileResponse getMyProfile() {
//        String currentEmail = getCurrentUserEmail();
//
//        User user = userRepository.findByEmail(currentEmail)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "User", "email", currentEmail));
//
//        return buildProfileResponse(user);
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    // Get All Users (Super Admin Only)
//    // ─────────────────────────────────────────────────────────────
//
//    /**
//     * Get all users in the system.
//     * Only SUPER_ADMIN can access this.
//     *
//     * Returns all roles including other admins.
//     * Sorted by role hierarchy then by name.
//     */
//    public List<UserResponse> getAllUsers() {
//        log.info("Super admin fetching all users");
//
//        return userRepository.findAll()
//                .stream()
//                .map(UserResponse::fromUser)
//                .collect(Collectors.toList());
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    // Get Users by Role (Super Admin + Manager)
//    // ─────────────────────────────────────────────────────────────
//
//    /**
//     * Get all users with a specific role.
//     *
//     * Access rules enforced here:
//     *   SUPER_ADMIN: can get users of ANY role
//     *   NOC_MANAGER: can only get NOC_ENGINEER users
//     *     (managers cannot see other managers or admins)
//     *
//     * @param requestedRole The role to filter by
//     * @param requesterEmail Email of the person making the request
//     */
//    public List<UserResponse> getUsersByRole(Role requestedRole,
//                                             String requesterEmail) {
//        User requester = userRepository.findByEmail(requesterEmail)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "User", "email", requesterEmail));
//
//        // Managers can only see engineers
//        if (requester.getRole() == Role.NOC_MANAGER) {
//            if (requestedRole != Role.NOC_ENGINEER) {
//                throw new UnauthorizedException(
//                        "Managers can only view NOC_ENGINEER profiles.");
//            }
//        }
//
//        return userRepository.findByRole(requestedRole)
//                .stream()
//                .map(UserResponse::fromUser)
//                .collect(Collectors.toList());
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    // Get Team / Subordinates
//    // ─────────────────────────────────────────────────────────────
//
//    /**
//     * Get the team of subordinates for the current user.
//     *
//     * SUPER_ADMIN:
//     *   Returns all managers AND all engineers
//     *   (everyone below super admin level)
//     *
//     * NOC_MANAGER:
//     *   Returns all engineers only
//     *   (NOC_ENGINEER role users)
//     *
//     * Other roles:
//     *   Returns empty list (no subordinates)
//     */
//    public List<UserResponse> getMyTeam() {
//        String currentEmail = getCurrentUserEmail();
//
//        User currentUser = userRepository.findByEmail(currentEmail)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "User", "email", currentEmail));
//
//        if (currentUser.getRole() == Role.SUPER_ADMIN) {
//            // Super admin sees all managers and engineers
//            return userRepository.findByRoleIn(
//                            List.of(Role.NOC_MANAGER,
//                                    Role.NOC_ENGINEER,
//                                    Role.READ_ONLY))
//                    .stream()
//                    .map(UserResponse::fromUser)
//                    .collect(Collectors.toList());
//        }
//
//        if (currentUser.getRole() == Role.NOC_MANAGER) {
//            // Manager sees all engineers
//            return userRepository.findByRole(Role.NOC_ENGINEER)
//                    .stream()
//                    .map(UserResponse::fromUser)
//                    .collect(Collectors.toList());
//        }
//
//        // Engineers and read-only have no subordinates
//        return List.of();
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    // Get Any User's Profile (Admin + Manager with restrictions)
//    // ─────────────────────────────────────────────────────────────
//
//    /**
//     * Get a specific user's full profile including activity stats.
//     *
//     * Access rules:
//     *   SUPER_ADMIN:
//     *     Can view anyone's profile.
//     *
//     *   NOC_MANAGER:
//     *     Can view their own profile.
//     *     Can view any NOC_ENGINEER profile.
//     *     Cannot view another manager's profile.
//     *     Cannot view super admin profile.
//     *
//     *   NOC_ENGINEER / READ_ONLY:
//     *     Can only view their own profile.
//     *
//     * @param targetUserId  ID of the user whose profile to view
//     * @param requesterEmail Email of the person making the request
//     */
//    public UserProfileResponse getUserProfile(String targetUserId,
//                                              String requesterEmail) {
//
//        User requester = userRepository.findByEmail(requesterEmail)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "User", "email", requesterEmail));
//
//        User targetUser = userRepository.findById(targetUserId)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "User", "id", targetUserId));
//
//        // Check access permissions
//        if (requester.getRole() == Role.SUPER_ADMIN) {
//            // Super admin can view anyone
//            log.debug("Super admin {} viewing profile of {}",
//                    requesterEmail, targetUser.getEmail());
//
//        } else if (requester.getRole() == Role.NOC_MANAGER) {
//            // Managers can view engineers only
//            // Or their own profile
//            boolean isOwnProfile = requester.getId()
//                    .equals(targetUser.getId());
//            boolean isEngineer = targetUser.getRole() == Role.NOC_ENGINEER
//                    || targetUser.getRole() == Role.READ_ONLY;
//
//            if (!isOwnProfile && !isEngineer) {
//                throw new UnauthorizedException(
//                        "Managers can only view NOC Engineer profiles " +
//                                "or their own profile.");
//            }
//
//        } else {
//            // Engineers and read-only can only view their own profile
//            if (!requester.getId().equals(targetUser.getId())) {
//                throw new UnauthorizedException(
//                        "You can only view your own profile.");
//            }
//        }
//
//        return buildProfileResponse(targetUser);
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    // Update Own Profile
//    // ─────────────────────────────────────────────────────────────
//
//    /**
//     * Update the current user's own profile (name only).
//     *
//     * All authenticated users can update their own name.
//     * Email cannot be changed (it is the login identifier).
//     * Role changes require SUPER_ADMIN via a separate endpoint.
//     */
//    public UserResponse updateMyProfile(UpdateProfileRequest request) {
//        String currentEmail = getCurrentUserEmail();
//
//        User user = userRepository.findByEmail(currentEmail)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "User", "email", currentEmail));
//
//        String oldName = user.getName();
//        user.setName(request.getName());
//        User savedUser = userRepository.save(user);
//
//        auditService.log(
//                currentEmail,
//                AuditAction.AUTH_REGISTER, // Re-using closest action
//                "User",
//                savedUser.getId(),
//                savedUser.getName(),
//                "Profile updated: name changed from '"
//                        + oldName + "' to '" + request.getName() + "'"
//        );
//
//        log.info("Profile updated for user: {}", currentEmail);
//
//        return UserResponse.fromUser(savedUser);
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    // Change Own Password
//    // ─────────────────────────────────────────────────────────────
//
//    /**
//     * Change the current user's password.
//     *
//     * Requires current password verification.
//     * If current password is wrong, throw BadRequestException.
//     *
//     * All authenticated users can change their own password.
//     */
//    public void changeMyPassword(ChangePasswordRequest request) {
//        String currentEmail = getCurrentUserEmail();
//
//        User user = userRepository.findByEmail(currentEmail)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "User", "email", currentEmail));
//
//        // Verify current password
//        if (!passwordEncoder.matches(request.getCurrentPassword(),
//                user.getPassword())) {
//            throw new BadRequestException(
//                    "Current password is incorrect.");
//        }
//
//        // Prevent same password
//        if (passwordEncoder.matches(request.getNewPassword(),
//                user.getPassword())) {
//            throw new BadRequestException(
//                    "New password must be different from current password.");
//        }
//
//        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
//        userRepository.save(user);
//
//        auditService.log(
//                currentEmail,
//                AuditAction.AUTH_REGISTER,
//                "User",
//                user.getId(),
//                user.getName(),
//                "Password changed"
//        );
//
//        log.info("Password changed for user: {}", currentEmail);
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    // Activate / Deactivate User (Super Admin Only)
//    // ─────────────────────────────────────────────────────────────
//
//    /**
//     * Activate a user account.
//     *
//     * Only SUPER_ADMIN can activate users.
//     * Activated users can log in again.
//     *
//     * @param userId ID of the user to activate
//     */
//    public UserResponse activateUser(String userId) {
//        String currentEmail = getCurrentUserEmail();
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "User", "id", userId));
//
//        if (user.isActive()) {
//            throw new BadRequestException(
//                    "User account is already active.");
//        }
//
//        user.setActive(true);
//        User savedUser = userRepository.save(user);
//
//        auditService.log(
//                currentEmail,
//                AuditAction.AUTH_REGISTER,
//                "User",
//                savedUser.getId(),
//                savedUser.getName(),
//                "User account activated"
//        );
//
//        log.info("User activated: {} by {}", user.getEmail(), currentEmail);
//
//        return UserResponse.fromUser(savedUser);
//    }
//
//    /**
//     * Deactivate a user account.
//     *
//     * Only SUPER_ADMIN can deactivate users.
//     * Deactivated users cannot log in.
//     * Their data is preserved (soft delete).
//     *
//     * Business rule:
//     *   Cannot deactivate your own account.
//     *   (Would lock yourself out of the system)
//     *
//     * @param userId ID of the user to deactivate
//     */
//    public UserResponse deactivateUser(String userId) {
//        String currentEmail = getCurrentUserEmail();
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "User", "id", userId));
//
//        // Cannot deactivate yourself
//        if (user.getEmail().equals(currentEmail)) {
//            throw new BadRequestException(
//                    "You cannot deactivate your own account.");
//        }
//
//        if (!user.isActive()) {
//            throw new BadRequestException(
//                    "User account is already inactive.");
//        }
//
//        user.setActive(false);
//        User savedUser = userRepository.save(user);
//
//        auditService.log(
//                currentEmail,
//                AuditAction.AUTH_REGISTER,
//                "User",
//                savedUser.getId(),
//                savedUser.getName(),
//                "User account deactivated"
//        );
//
//        log.info("User deactivated: {} by {}",
//                user.getEmail(), currentEmail);
//
//        return UserResponse.fromUser(savedUser);
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    // Change User Role (Super Admin Only)
//    // ─────────────────────────────────────────────────────────────
//
//    /**
//     * Change a user's role.
//     *
//     * Only SUPER_ADMIN can change roles.
//     *
//     * Business rules:
//     *   - Cannot change your own role
//     *     (could accidentally remove your own admin access)
//     *   - Cannot promote someone to SUPER_ADMIN
//     *     (super admin accounts are created manually)
//     *
//     * @param userId  ID of the user whose role to change
//     * @param request The new role
//     */
//    public UserResponse updateUserRole(String userId,
//                                       UpdateUserRoleRequest request) {
//        String currentEmail = getCurrentUserEmail();
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "User", "id", userId));
//
//        // Cannot change your own role
//        if (user.getEmail().equals(currentEmail)) {
//            throw new BadRequestException(
//                    "You cannot change your own role.");
//        }
//
//        // Cannot promote to SUPER_ADMIN via API
//        if (request.getRole() == Role.SUPER_ADMIN) {
//            throw new BadRequestException(
//                    "Cannot promote users to SUPER_ADMIN via API. " +
//                            "Super admin accounts must be created directly.");
//        }
//
//        Role oldRole = user.getRole();
//        user.setRole(request.getRole());
//        User savedUser = userRepository.save(user);
//
//        auditService.log(
//                currentEmail,
//                AuditAction.AUTH_REGISTER,
//                "User",
//                savedUser.getId(),
//                savedUser.getName(),
//                "Role changed from " + oldRole + " to " + request.getRole()
//        );
//
//        log.info("Role changed for user: {} from {} to {} by {}",
//                user.getEmail(), oldRole, request.getRole(), currentEmail);
//
//        return UserResponse.fromUser(savedUser);
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    // Get All Active Engineers (for ticket assignment)
//    // ─────────────────────────────────────────────────────────────
//
//    /**
//     * Get all active engineers available for ticket assignment.
//     *
//     * Used by:
//     *   - Managers when assigning tickets (need to pick an engineer)
//     *   - Frontend dropdown for ticket assignment
//     *
//     * Only returns ACTIVE users with NOC_ENGINEER role.
//     * Inactive engineers should not receive new tickets.
//     */
//    public List<UserResponse> getActiveEngineers() {
//        return userRepository.findByRoleAndActive(Role.NOC_ENGINEER, true)
//                .stream()
//                .map(UserResponse::fromUser)
//                .collect(Collectors.toList());
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    // Admin Statistics
//    // ─────────────────────────────────────────────────────────────
//
//    /**
//     * Get user statistics for admin dashboard.
//     *
//     * Returns counts of users by role and active status.
//     * Used by SUPER_ADMIN for system overview.
//     */
//    public UserStatsResponse getUserStats() {
//        UserStatsResponse stats = new UserStatsResponse();
//
//        stats.setTotalUsers(userRepository.count());
//        stats.setActiveUsers(userRepository.countByActive(true));
//        stats.setInactiveUsers(userRepository.countByActive(false));
//        stats.setTotalAdmins(userRepository.countByRole(Role.SUPER_ADMIN));
//        stats.setTotalManagers(userRepository.countByRole(Role.NOC_MANAGER));
//        stats.setTotalEngineers(userRepository.countByRole(Role.NOC_ENGINEER));
//        stats.setTotalReadOnly(userRepository.countByRole(Role.READ_ONLY));
//
//        return stats;
//    }
//
//    // ─────────────────────────────────────────────────────────────
//    // Private Helpers
//    // ─────────────────────────────────────────────────────────────
//
//    /**
//     * Build a full UserProfileResponse including activity stats.
//     *
//     * Queries alarm and ticket repositories to get:
//     *   - How many alarms this user raised
//     *   - How many tickets they created
//     *   - How many tickets they resolved
//     *   - How many tickets are currently assigned to them
//     */
//    private UserProfileResponse buildProfileResponse(User user) {
//        UserProfileResponse profile =
//                UserProfileResponse.fromUser(user);
//
//        // Count alarms raised by this user
//        // We use the raisedBy field which stores email
//        long alarmsRaised = alarmRepository.findAll()
//                .stream()
//                .filter(a -> user.getEmail().equals(a.getRaisedBy()))
//                .count();
//        profile.setAlarmsRaised(alarmsRaised);
//
//        // Count tickets created by this user
//        long ticketsCreated = ticketRepository.findAll()
//                .stream()
//                .filter(t -> user.getEmail().equals(t.getCreatedBy()))
//                .count();
//        profile.setTicketsCreated(ticketsCreated);
//
//        // Count tickets resolved by this user (by email in resolvedBy)
//        long ticketsResolved = alarmRepository.findAll()
//                .stream()
//                .filter(a -> user.getEmail().equals(a.getResolvedBy()))
//                .count();
//        profile.setTicketsResolved(ticketsResolved);
//
//        // Count tickets currently assigned to this user (by user ID)
//        long ticketsAssigned = ticketRepository
//                .findByAssignedTo(user.getId())
//                .stream()
//                .filter(t -> t.getStatus() ==
//                        com.teleops.teleops_ai.ticket.model.TicketStatus.OPEN
//                        || t.getStatus() ==
//                        com.teleops.teleops_ai.ticket.model.TicketStatus.IN_PROGRESS)
//                .count();
//        profile.setTicketsAssigned(ticketsAssigned);
//
//        return profile;
//    }
//
//    /**
//     * Get the email of the currently authenticated user.
//     */
//    private String getCurrentUserEmail() {
//        return SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getName();
//    }
//}


package com.teleops.teleops_ai.auth.service;

import com.teleops.teleops_ai.alarm.repository.AlarmRepository;
import com.teleops.teleops_ai.audit.model.AuditAction;
import com.teleops.teleops_ai.audit.service.AuditService;
import com.teleops.teleops_ai.auth.dto.*;
import com.teleops.teleops_ai.auth.model.Role;
import com.teleops.teleops_ai.auth.model.User;
import com.teleops.teleops_ai.auth.repository.UserRepository;
import com.teleops.teleops_ai.common.exception.BadRequestException;
import com.teleops.teleops_ai.common.exception.ResourceNotFoundException;
import com.teleops.teleops_ai.common.exception.UnauthorizedException;
import com.teleops.teleops_ai.ticket.model.TicketStatus;
import com.teleops.teleops_ai.ticket.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User Management Service
 *
 * Handles all user management operations:
 *   1. View all users (Super Admin only)
 *   2. View subordinates (Manager views engineers, Admin views all)
 *   3. View any user profile with activity stats
 *   4. Update own profile (name)
 *   5. Change own password
 *   6. Activate/deactivate users (Super Admin only)
 *   7. Change user role (Super Admin only)
 *
 * Access control rules:
 *   SUPER_ADMIN:
 *     - Can view ALL users
 *     - Can view ANY user's profile
 *     - Can activate/deactivate any user
 *     - Can change any user's role
 *
 *   NOC_MANAGER:
 *     - Can view all NOC_ENGINEER profiles
 *     - Can view their own profile
 *     - Cannot view other managers' or admin profiles
 *     - Cannot activate/deactivate or change roles
 *
 *   NOC_ENGINEER / READ_ONLY:
 *     - Can only view and update their own profile
 */
@Service
public class UserService {

    private static final Logger log =
            LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final AlarmRepository alarmRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository,
                       AlarmRepository alarmRepository,
                       TicketRepository ticketRepository,
                       PasswordEncoder passwordEncoder,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.alarmRepository = alarmRepository;
        this.ticketRepository = ticketRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    // ─────────────────────────────────────────────────────────────
    // Get Current User's Own Profile
    // ─────────────────────────────────────────────────────────────

    /**
     * Get the currently authenticated user's full profile
     * including activity statistics.
     */
    public UserProfileResponse getMyProfile() {
        String currentEmail = getCurrentUserEmail();

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", currentEmail));

        return buildProfileResponse(user);
    }

    // ─────────────────────────────────────────────────────────────
    // Get All Users (Super Admin Only)
    // ─────────────────────────────────────────────────────────────

    /**
     * Get all users in the system.
     * Only SUPER_ADMIN should call this (enforced in controller).
     */
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");

        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    // Get Users by Role
    // ─────────────────────────────────────────────────────────────

    /**
     * Get all users with a specific role.
     *
     * SUPER_ADMIN: can get users of ANY role.
     * NOC_MANAGER: can only get NOC_ENGINEER users.
     *
     * @param requestedRole  The role to filter by
     * @param requesterEmail Email of the requesting user
     */
    public List<UserResponse> getUsersByRole(Role requestedRole,
                                             String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", requesterEmail));

        // Managers can only view engineers
        if (requester.getRole() == Role.NOC_MANAGER) {
            if (requestedRole != Role.NOC_ENGINEER) {
                throw new UnauthorizedException(
                        "Managers can only view NOC_ENGINEER profiles.");
            }
        }

        return userRepository.findByRole(requestedRole)
                .stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    // Get Team / Subordinates
    // ─────────────────────────────────────────────────────────────

    /**
     * Get the team subordinates for the current authenticated user.
     *
     * SUPER_ADMIN   → returns managers + engineers + read-only
     * NOC_MANAGER   → returns engineers only
     * Others        → returns empty list
     */
    public List<UserResponse> getMyTeam() {
        String currentEmail = getCurrentUserEmail();

        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", currentEmail));

        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            return userRepository.findByRoleIn(
                            List.of(Role.NOC_MANAGER,
                                    Role.NOC_ENGINEER,
                                    Role.READ_ONLY))
                    .stream()
                    .map(UserResponse::fromUser)
                    .collect(Collectors.toList());
        }

        if (currentUser.getRole() == Role.NOC_MANAGER) {
            return userRepository.findByRole(Role.NOC_ENGINEER)
                    .stream()
                    .map(UserResponse::fromUser)
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    // ─────────────────────────────────────────────────────────────
    // Get Any User's Profile
    // ─────────────────────────────────────────────────────────────

    /**
     * Get a specific user's full profile including activity stats.
     *
     * Access rules:
     *   SUPER_ADMIN  → any profile
     *   NOC_MANAGER  → own profile + any engineer profile
     *   Others       → own profile only
     *
     * @param targetUserId   ID of user to view
     * @param requesterEmail Email of the requesting user
     */
    public UserProfileResponse getUserProfile(String targetUserId,
                                              String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", requesterEmail));

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "id", targetUserId));

        if (requester.getRole() == Role.SUPER_ADMIN) {
            // Super admin can view anyone
            log.debug("Super admin {} viewing profile of {}",
                    requesterEmail, targetUser.getEmail());

        } else if (requester.getRole() == Role.NOC_MANAGER) {
            boolean isOwnProfile = requester.getId()
                    .equals(targetUser.getId());
            boolean isSubordinate =
                    targetUser.getRole() == Role.NOC_ENGINEER ||
                            targetUser.getRole() == Role.READ_ONLY;

            if (!isOwnProfile && !isSubordinate) {
                throw new UnauthorizedException(
                        "Managers can only view NOC Engineer profiles " +
                                "or their own profile.");
            }

        } else {
            // Engineers and read-only can only view their own profile
            if (!requester.getId().equals(targetUser.getId())) {
                throw new UnauthorizedException(
                        "You can only view your own profile.");
            }
        }

        return buildProfileResponse(targetUser);
    }

    // ─────────────────────────────────────────────────────────────
    // Update Own Profile
    // ─────────────────────────────────────────────────────────────

    /**
     * Update the current user's own name.
     * All authenticated users can do this.
     */
    public UserResponse updateMyProfile(UpdateProfileRequest request) {
        String currentEmail = getCurrentUserEmail();

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", currentEmail));

        String oldName = user.getName();
        user.setName(request.getName());
        User savedUser = userRepository.save(user);

        auditService.log(
                currentEmail,
                AuditAction.USER_PROFILE_UPDATED,
                "User",
                savedUser.getId(),
                savedUser.getName(),
                "Name changed from '" + oldName
                        + "' to '" + request.getName() + "'"
        );

        log.info("Profile updated for user: {}", currentEmail);

        return UserResponse.fromUser(savedUser);
    }

    // ─────────────────────────────────────────────────────────────
    // Change Own Password
    // ─────────────────────────────────────────────────────────────

    /**
     * Change the current user's password.
     * Requires current password for verification.
     */
    public void changeMyPassword(ChangePasswordRequest request) {
        String currentEmail = getCurrentUserEmail();

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", currentEmail));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(),
                user.getPassword())) {
            throw new BadRequestException(
                    "Current password is incorrect.");
        }

        // Prevent reuse of same password
        if (passwordEncoder.matches(request.getNewPassword(),
                user.getPassword())) {
            throw new BadRequestException(
                    "New password must be different from current password.");
        }

        user.setPassword(
                passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditService.log(
                currentEmail,
                AuditAction.USER_PASSWORD_CHANGED,
                "User",
                user.getId(),
                user.getName(),
                "Password changed successfully"
        );

        log.info("Password changed for user: {}", currentEmail);
    }

    // ─────────────────────────────────────────────────────────────
    // Activate User (Super Admin Only)
    // ─────────────────────────────────────────────────────────────

    /**
     * Activate a deactivated user account.
     * Only SUPER_ADMIN (enforced in controller).
     */
    public UserResponse activateUser(String userId) {
        String currentEmail = getCurrentUserEmail();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "id", userId));

        if (user.isActive()) {
            throw new BadRequestException(
                    "User account is already active.");
        }

        user.setActive(true);
        User savedUser = userRepository.save(user);

        auditService.log(
                currentEmail,
                AuditAction.USER_ACTIVATED,
                "User",
                savedUser.getId(),
                savedUser.getName(),
                "User account activated by " + currentEmail
        );

        log.info("User activated: {} by {}",
                user.getEmail(), currentEmail);

        return UserResponse.fromUser(savedUser);
    }

    // ─────────────────────────────────────────────────────────────
    // Deactivate User (Super Admin Only)
    // ─────────────────────────────────────────────────────────────

    /**
     * Deactivate a user account (soft disable).
     * Only SUPER_ADMIN (enforced in controller).
     *
     * Cannot deactivate own account.
     * Deactivated users cannot log in.
     * All their data is preserved.
     */
    public UserResponse deactivateUser(String userId) {
        String currentEmail = getCurrentUserEmail();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "id", userId));

        // Cannot deactivate yourself
        if (user.getEmail().equals(currentEmail)) {
            throw new BadRequestException(
                    "You cannot deactivate your own account.");
        }

        if (!user.isActive()) {
            throw new BadRequestException(
                    "User account is already inactive.");
        }

        user.setActive(false);
        User savedUser = userRepository.save(user);

        auditService.log(
                currentEmail,
                AuditAction.USER_DEACTIVATED,
                "User",
                savedUser.getId(),
                savedUser.getName(),
                "User account deactivated by " + currentEmail
        );

        log.info("User deactivated: {} by {}",
                user.getEmail(), currentEmail);

        return UserResponse.fromUser(savedUser);
    }

    // ─────────────────────────────────────────────────────────────
    // Change User Role (Super Admin Only)
    // ─────────────────────────────────────────────────────────────

    /**
     * Change a user's role.
     * Only SUPER_ADMIN (enforced in controller).
     *
     * Restrictions:
     *   - Cannot change your own role
     *   - Cannot promote to SUPER_ADMIN via API
     */
    public UserResponse updateUserRole(String userId,
                                       UpdateUserRoleRequest request) {
        String currentEmail = getCurrentUserEmail();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "id", userId));

        // Cannot change your own role
        if (user.getEmail().equals(currentEmail)) {
            throw new BadRequestException(
                    "You cannot change your own role.");
        }

        // Cannot promote to SUPER_ADMIN via API
        if (request.getRole() == Role.SUPER_ADMIN) {
            throw new BadRequestException(
                    "Cannot promote users to SUPER_ADMIN via API. " +
                            "Super admin accounts must be created directly.");
        }

        Role oldRole = user.getRole();
        user.setRole(request.getRole());
        User savedUser = userRepository.save(user);

        auditService.log(
                currentEmail,
                AuditAction.USER_ROLE_CHANGED,
                "User",
                savedUser.getId(),
                savedUser.getName(),
                "Role changed from " + oldRole
                        + " to " + request.getRole()
                        + " by " + currentEmail
        );

        log.info("Role changed: {} → {} for {} by {}",
                oldRole, request.getRole(),
                user.getEmail(), currentEmail);

        return UserResponse.fromUser(savedUser);
    }

    // ─────────────────────────────────────────────────────────────
    // Get Active Engineers (for ticket assignment)
    // ─────────────────────────────────────────────────────────────

    /**
     * Get all active engineers available for ticket assignment.
     * Managers use this when assigning tickets.
     */
    public List<UserResponse> getActiveEngineers() {
        return userRepository
                .findByRoleAndActive(Role.NOC_ENGINEER, true)
                .stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    // User Statistics
    // ─────────────────────────────────────────────────────────────

    /**
     * Get aggregated user statistics for admin dashboard.
     */
    public UserStatsResponse getUserStats() {
        UserStatsResponse stats = new UserStatsResponse();

        stats.setTotalUsers(userRepository.count());
        stats.setActiveUsers(userRepository.countByActive(true));
        stats.setInactiveUsers(userRepository.countByActive(false));
        stats.setTotalAdmins(
                userRepository.countByRole(Role.SUPER_ADMIN));
        stats.setTotalManagers(
                userRepository.countByRole(Role.NOC_MANAGER));
        stats.setTotalEngineers(
                userRepository.countByRole(Role.NOC_ENGINEER));
        stats.setTotalReadOnly(
                userRepository.countByRole(Role.READ_ONLY));

        return stats;
    }

    // ─────────────────────────────────────────────────────────────
    // Private Helpers
    // ─────────────────────────────────────────────────────────────

    /**
     * Build UserProfileResponse with activity statistics.
     *
     * raisedBy and resolvedBy on Alarm store email (String).
     * assignedTo on Ticket stores userId (String).
     * createdBy on Ticket stores email (String).
     */
    private UserProfileResponse buildProfileResponse(User user) {
        UserProfileResponse profile =
                UserProfileResponse.fromUser(user);

        // Alarms raised by this user (email stored in raisedBy)
        long alarmsRaised = alarmRepository.findAll()
                .stream()
                .filter(a -> user.getEmail().equals(a.getRaisedBy()))
                .count();
        profile.setAlarmsRaised(alarmsRaised);

        // Tickets created by this user (email stored in createdBy)
        long ticketsCreated = ticketRepository.findAll()
                .stream()
                .filter(t -> user.getEmail().equals(t.getCreatedBy()))
                .count();
        profile.setTicketsCreated(ticketsCreated);

        // Alarms resolved by this user (email stored in resolvedBy)
        long alarmsResolved = alarmRepository.findAll()
                .stream()
                .filter(a -> user.getEmail().equals(a.getResolvedBy()))
                .count();
        profile.setTicketsResolved(alarmsResolved);

        // Tickets currently assigned to this user (userId in assignedTo)
        // and still open or in-progress
        long ticketsAssigned = ticketRepository
                .findByAssignedTo(user.getId())
                .stream()
                .filter(t ->
                        t.getStatus() == TicketStatus.OPEN ||
                                t.getStatus() == TicketStatus.IN_PROGRESS)
                .count();
        profile.setTicketsAssigned(ticketsAssigned);

        return profile;
    }

    /**
     * Get email of currently authenticated user from SecurityContext.
     */
    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }
}