package com.teleops.teleops_ai.config;

import com.teleops.teleops_ai.security.JwtAuthFilter;
import com.teleops.teleops_ai.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Production Security Configuration
 *
 * This replaces the temporary SecurityConfig from Phase 4.
 *
 * @EnableWebSecurity:
 *   Activates Spring Security's web security support.
 *   Required for the SecurityFilterChain to be applied.
 *
 * @EnableMethodSecurity:
 *   Enables @PreAuthorize annotations on service methods.
 *   prePostEnabled = true (default in Spring Boot 3)
 *   Without this, @PreAuthorize annotations are silently ignored.
 *
 * Key design decisions:
 *
 *   1. STATELESS sessions:
 *      We use JWT. No server-side session is ever created.
 *      Spring Security must not create or use HttpSession.
 *
 *   2. CSRF disabled:
 *      CSRF protection is for session-based authentication.
 *      JWT is stateless and not vulnerable to CSRF attacks.
 *      Safe to disable for REST APIs.
 *
 *   3. Public endpoints:
 *      /api/v1/auth/**     = login and register (no token needed)
 *      /swagger-ui/**      = API documentation
 *      /api-docs/**        = OpenAPI spec
 *      /actuator/health    = health check for Render
 *
 *   4. Everything else requires authentication.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          UserDetailsServiceImpl userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    /**
     * The main security filter chain.
     *
     * Order of filter chain:
     *   1. CORS filter
     *   2. JWT Auth Filter (our custom filter)
     *   3. UsernamePasswordAuthenticationFilter (standard Spring filter)
     *   ... rest of Spring Security filters
     *
     * addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
     *   Inserts our JWT filter BEFORE Spring's default auth filter.
     *   This ensures JWT is validated before Spring tries other auth methods.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // Disable CSRF — not needed for stateless JWT REST APIs
                .csrf(AbstractHttpConfigurer::disable)

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Define which endpoints are public vs protected
                .authorizeHttpRequests(auth -> auth

                        // Public endpoints — no token required
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // Stateless — Spring Security will NOT create HTTP sessions
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Use our DaoAuthenticationProvider
                .authenticationProvider(authenticationProvider())

                // Add our JWT filter before the default auth filter
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Authentication Provider
     *
     * DaoAuthenticationProvider:
     *   Spring's standard authentication provider for
     *   username/password authentication against a database.
     *
     *   It uses:
     *     - UserDetailsService to load user by email
     *     - PasswordEncoder to verify the password hash
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager
     *
     * Required by AuthService to programmatically authenticate
     * a user during login (email + password validation).
     *
     * We get it from Spring's AuthenticationConfiguration
     * instead of building it manually.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Password Encoder — BCrypt with strength 12
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * CORS Configuration
     *
     * Cross-Origin Resource Sharing:
     *   Allows our frontend (running on a different domain)
     *   to call our backend API.
     *
     *   In production:
     *     Replace "*" with your actual frontend domain:
     *     "https://your-app.vercel.app"
     *
     *   In development:
     *     Allow localhost:3000, localhost:5500, etc.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed origins — update for production
        configuration.setAllowedOriginPatterns(List.of("*"));

        // Standard HTTP methods for REST API
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Allow Authorization header (our JWT) and Content-Type
        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Accept"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}