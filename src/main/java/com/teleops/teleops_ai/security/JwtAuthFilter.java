package com.teleops.teleops_ai.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 *
 * Extends OncePerRequestFilter:
 *   Guarantees this filter runs exactly ONCE per request.
 *   This is important — without this, filters can run multiple times.
 *
 * What this filter does on EVERY incoming request:
 *   1. Check if Authorization header exists and starts with "Bearer "
 *   2. Extract the JWT token
 *   3. Extract the email from the token
 *   4. If no authentication set yet in SecurityContext:
 *      a. Load user from database
 *      b. Validate token against user
 *      c. Set authentication in SecurityContext
 *   5. Continue the filter chain
 *
 * What happens if token is missing or invalid:
 *   - We simply do NOT set authentication
 *   - The next filter or endpoint will return 401/403
 *   - We never throw exceptions from this filter directly
 *
 * SecurityContextHolder:
 *   Spring's thread-local storage for the current user.
 *   Once we set authentication here, all downstream code
 *   (controllers, services) can access the current user via:
 *   SecurityContextHolder.getContext().getAuthentication()
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthFilter(JwtService jwtService,
                         UserDetailsServiceImpl userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // ── Step 1: Check for Authorization header ───────────────
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No JWT present — pass through without setting auth
            // Spring Security will handle the missing auth downstream
            filterChain.doFilter(request, response);
            return;
        }

        // ── Step 2: Extract token (remove "Bearer " prefix) ──────
        final String jwt = authHeader.substring(7);
        final String userEmail;

        try {
            userEmail = jwtService.extractEmail(jwt);
        } catch (Exception e) {
            // Token is malformed or signature is invalid
            log.warn("Invalid JWT token: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // ── Step 3: Set authentication if not already set ────────
        // We check getAuthentication() == null to avoid
        // re-processing if auth was already set (e.g., by another filter)
        if (userEmail != null &&
                SecurityContextHolder.getContext()
                        .getAuthentication() == null) {

            UserDetails userDetails =
                    this.userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Create authentication token with:
                //   principal   = UserDetails (the user)
                //   credentials = null (we don't store passwords here)
                //   authorities = user's roles
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // Attach request details (IP, session ID) to auth token
                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request));

                // Set in SecurityContext — user is now authenticated
                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);

                log.debug("Authenticated user: {} for path: {}",
                        userEmail, request.getRequestURI());
            }
        }

        // ── Step 4: Continue filter chain ────────────────────────
        filterChain.doFilter(request, response);
    }
}