package com.teleops.teleops_ai.auth.repository;

import com.teleops.teleops_ai.auth.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Repository
 *
 * MongoRepository<User, String>:
 *   User   = the document type
 *   String = the type of the @Id field
 *
 * Spring Data auto-implements this interface at runtime.
 * We never write the implementation — Spring generates it.
 *
 * Built-in methods we get for free:
 *   save(user)          → insert or update
 *   findById(id)        → find by _id
 *   findAll()           → get all users
 *   deleteById(id)      → delete by _id
 *   count()             → total count
 *   existsById(id)      → boolean check
 *
 * Custom query methods use Spring Data naming conventions:
 *   findByEmail         → generates: { email: ? }
 *   existsByEmail       → generates: { email: ? } count > 0
 *
 * No SQL. No @Query needed for simple lookups.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Find user by email address.
     * Used during login to load the user for authentication.
     *
     * Returns Optional to force callers to handle "not found" case.
     * This prevents NullPointerExceptions.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email already exists.
     * Used during registration to prevent duplicates.
     *
     * More efficient than findByEmail() when we only need
     * a boolean answer — does not load the full document.
     */
    boolean existsByEmail(String email);
}