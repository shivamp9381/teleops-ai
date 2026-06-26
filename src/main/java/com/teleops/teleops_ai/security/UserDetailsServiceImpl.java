package com.teleops.teleops_ai.security;

import com.teleops.teleops_ai.auth.model.User;
import com.teleops.teleops_ai.auth.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UserDetailsService Implementation
 *
 * This is the bridge between Spring Security and our User model.
 *
 * Spring Security does not know about our User class.
 * It works with its own UserDetails interface.
 *
 * This class:
 *   1. Loads a user from MongoDB by email
 *   2. Converts our User to Spring's UserDetails format
 *   3. Maps our Role enum to Spring's GrantedAuthority
 *
 * Spring Security calls loadUserByUsername() during:
 *   - Password-based authentication
 *   - JWT filter (to load user context from token)
 *
 * The "username" in Spring Security = our "email".
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Load user by email (Spring calls this "username").
     *
     * We wrap the role with "ROLE_" prefix because:
     *   hasRole('NOC_MANAGER') checks for authority "ROLE_NOC_MANAGER"
     *
     * We return org.springframework.security.core.userdetails.User
     * (Spring's built-in implementation of UserDetails).
     *
     * The User constructor takes:
     *   username  = email
     *   password  = bcrypt hash (Spring validates this)
     *   authorities = list of granted roles
     */
    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));

        // Map our Role enum to Spring Security's GrantedAuthority
        // The "ROLE_" prefix is required for hasRole() to work
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isActive(),    // enabled
                true,               // accountNonExpired
                true,               // credentialsNonExpired
                true,               // accountNonLocked
                List.of(authority)
        );
    }
}