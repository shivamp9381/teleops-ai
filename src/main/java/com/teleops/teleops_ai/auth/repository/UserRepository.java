//package com.teleops.teleops_ai.auth.repository;
//
//import com.teleops.teleops_ai.auth.model.User;
//import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.Optional;
//
///**
// * User Repository
// *
// * MongoRepository<User, String>:
// *   User   = the document type
// *   String = the type of the @Id field
// *
// * Spring Data auto-implements this interface at runtime.
// * We never write the implementation — Spring generates it.
// *
// * Built-in methods we get for free:
// *   save(user)          → insert or update
// *   findById(id)        → find by _id
// *   findAll()           → get all users
// *   deleteById(id)      → delete by _id
// *   count()             → total count
// *   existsById(id)      → boolean check
// *
// * Custom query methods use Spring Data naming conventions:
// *   findByEmail         → generates: { email: ? }
// *   existsByEmail       → generates: { email: ? } count > 0
// *
// * No SQL. No @Query needed for simple lookups.
// */
//@Repository
//public interface UserRepository extends MongoRepository<User, String> {
//
//    /**
//     * Find user by email address.
//     * Used during login to load the user for authentication.
//     *
//     * Returns Optional to force callers to handle "not found" case.
//     * This prevents NullPointerExceptions.
//     */
//    Optional<User> findByEmail(String email);
//
//    /**
//     * Check if email already exists.
//     * Used during registration to prevent duplicates.
//     *
//     * More efficient than findByEmail() when we only need
//     * a boolean answer — does not load the full document.
//     */
//    boolean existsByEmail(String email);
//}



package com.teleops.teleops_ai.auth.repository;

import com.teleops.teleops_ai.auth.model.Role;
import com.teleops.teleops_ai.auth.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Repository
 *
 * Extended with role-based queries for user management features.
 *
 * New queries:
 *   findByRole         = get all users with a specific role
 *   findByRoleIn       = get users with any of the given roles
 *   findByActive       = get active or inactive users
 *   findByRoleAndActive = get active users of a specific role
 *
 * Used by:
 *   UserService        = user management operations
 *   TicketService      = validate engineer assignment
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Find user by email address.
     * Used during login and token refresh.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email already exists.
     * Used during registration.
     */
    boolean existsByEmail(String email);

    /**
     * Find all users with a specific role.
     * Used by managers to view their team (engineers).
     * Used by super admin to view all managers.
     */
    List<User> findByRole(Role role);

    /**
     * Find all users with any of the specified roles.
     * Used by super admin to view all staff.
     * Example: findByRoleIn([NOC_MANAGER, NOC_ENGINEER])
     */
    List<User> findByRoleIn(List<Role> roles);

    /**
     * Find all active or inactive users.
     * Used by admin to audit disabled accounts.
     */
    List<User> findByActive(boolean active);

    /**
     * Find users by role and active status.
     * Used by manager: "show all active engineers on my team"
     */
    List<User> findByRoleAndActive(Role role, boolean active);

    /**
     * Count users by role.
     * Used for admin dashboard statistics.
     */
    long countByRole(Role role);

    /**
     * Count active users.
     * Used for admin dashboard statistics.
     */
    long countByActive(boolean active);
}