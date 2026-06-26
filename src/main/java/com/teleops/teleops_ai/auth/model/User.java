package com.teleops.teleops_ai.auth.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * User MongoDB Document
 *
 * @Document(collection = "users")
 *   Maps this class to the "users" collection in MongoDB.
 *   Without this, Spring Data does not know which collection to use.
 *
 * @Id
 *   Maps to MongoDB's _id field.
 *   MongoDB auto-generates this as an ObjectId string.
 *
 * @Indexed(unique = true)
 *   Creates a unique index on the email field in MongoDB.
 *   Prevents duplicate accounts at the database level.
 *   This is a database-enforced constraint, not just application logic.
 *
 * @CreatedDate / @LastModifiedDate
 *   Automatically populated by @EnableMongoAuditing in main class.
 *   We never set these manually.
 *
 * No Lombok:
 *   As per project rules, we write constructors and getters manually.
 *   This is intentional — you understand what you write.
 */
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String email;

    /**
     * NEVER store plaintext passwords.
     * This field stores the BCrypt hash.
     * The actual password is never stored anywhere.
     */
    private String password;

    private Role role;

    /**
     * Controls whether the user can log in.
     * Admins can disable accounts without deleting them.
     */
    private boolean active = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────

    public User() {
    }

    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = true;
    }

    // ─────────────────────────────────────────
    // Getters and Setters
    // ─────────────────────────────────────────

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}