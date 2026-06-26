package com.teleops.teleops_ai.auth.service;

import com.teleops.teleops_ai.auth.dto.AuthResponse;
import com.teleops.teleops_ai.auth.dto.LoginRequest;
import com.teleops.teleops_ai.auth.dto.RegisterRequest;
import com.teleops.teleops_ai.auth.dto.UserResponse;
import com.teleops.teleops_ai.auth.model.Role;
import com.teleops.teleops_ai.auth.model.User;
import com.teleops.teleops_ai.auth.repository.UserRepository;
import com.teleops.teleops_ai.common.exception.BadRequestException;
import com.teleops.teleops_ai.common.exception.DuplicateResourceException;
import com.teleops.teleops_ai.common.exception.ResourceNotFoundException;
import com.teleops.teleops_ai.security.JwtService;
import com.teleops.teleops_ai.security.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Authentication Service
 *
 * All business logic for registration, login,
 * token refresh, and profile retrieval.
 */
@Service
public class AuthService {

    private static final Logger log =
            LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       UserDetailsServiceImpl userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    // ─────────────────────────────────────────
    // Register
    // ─────────────────────────────────────────

    /**
     * Register a new user.
     *
     * Steps:
     *   1. Check email is not already taken
     *   2. Hash the password with BCrypt
     *   3. Save user to MongoDB
     *   4. Generate and return JWT tokens
     */
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "An account with this email already exists: "
                            + request.getEmail());
        }

        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getRole() != null
                        ? request.getRole()
                        : Role.NOC_ENGINEER
        );

        User savedUser = userRepository.save(user);
        log.info("New user registered: {} with role: {}",
                savedUser.getEmail(), savedUser.getRole());

        return generateAuthResponse(savedUser);
    }

    // ─────────────────────────────────────────
    // Login
    // ─────────────────────────────────────────

    /**
     * Authenticate user and return JWT tokens.
     *
     * AuthenticationManager.authenticate() does two things:
     *   1. Loads user by email via UserDetailsService
     *   2. Verifies the password using BCryptPasswordEncoder
     *
     * Throws BadCredentialsException if credentials are wrong.
     * This is caught by our GlobalExceptionHandler → 401 response.
     */
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", request.getEmail()));

        log.info("User logged in: {}", user.getEmail());

        return generateAuthResponse(user);
    }

    // ─────────────────────────────────────────
    // Refresh Token
    // ─────────────────────────────────────────

    /**
     * Issue a new access token using a valid refresh token.
     *
     * Client sends the refresh token when access token expires.
     * We validate, then issue a fresh access token only.
     * The refresh token itself is NOT replaced here.
     */
    public AuthResponse refreshToken(String refreshToken) {

        final String userEmail;

        try {
            userEmail = jwtService.extractEmail(refreshToken);
        } catch (Exception e) {
            throw new BadRequestException("Invalid refresh token.");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", userEmail));

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(userEmail);

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new BadRequestException(
                    "Refresh token is expired or invalid.");
        }

        // Generate a new access token only
        String newAccessToken = jwtService.generateAccessToken(userDetails);

        log.info("Token refreshed for user: {}", userEmail);

        return new AuthResponse(
                newAccessToken,
                refreshToken,       // Return same refresh token
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    // ─────────────────────────────────────────
    // Get Current User Profile
    // ─────────────────────────────────────────

    /**
     * Return the authenticated user's profile.
     *
     * The email is extracted from the JWT in the controller
     * using Spring Security's authentication context.
     */
    public UserResponse getCurrentUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", email));

        return UserResponse.fromUser(user);
    }

    // ─────────────────────────────────────────
    // Private Helpers
    // ─────────────────────────────────────────

    /**
     * Generate both tokens and build the AuthResponse.
     *
     * Used by both register() and login() to avoid duplication.
     * This follows the DRY principle.
     */
    private AuthResponse generateAuthResponse(User user) {

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(user.getEmail());

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}