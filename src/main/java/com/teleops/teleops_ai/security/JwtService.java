package com.teleops.teleops_ai.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Service
 *
 * Responsible for all JWT operations:
 *   1. Generate access tokens
 *   2. Generate refresh tokens
 *   3. Extract claims (email, role, expiry) from token
 *   4. Validate token signature and expiry
 *
 * JWT Structure:
 *   header.payload.signature
 *
 *   Header:  algorithm (HS256) and token type
 *   Payload: claims (email, role, issued at, expiry)
 *   Signature: HMAC-SHA256 of header + payload using secret key
 *
 * The signature prevents tampering.
 * If anyone modifies the payload, the signature becomes invalid.
 *
 * Using JJWT 0.12.x API (note: API changed from 0.11.x).
 */
@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${application.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // ─────────────────────────────────────────
    // Token Generation
    // ─────────────────────────────────────────

    /**
     * Generate an access token for the authenticated user.
     *
     * We embed the user's role in the token claims so we do not
     * need to hit the database on every request to check the role.
     * The role is verified from the token itself.
     */
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        // Store the role as a claim in the JWT payload
        extraClaims.put("role",
                userDetails.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority());
        return buildToken(extraClaims, userDetails, accessTokenExpiration);
    }

    /**
     * Generate a refresh token.
     *
     * Refresh tokens carry no extra claims — just the subject (email)
     * and expiry. Their only purpose is to get a new access token.
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshTokenExpiration);
    }

    /**
     * Core token builder.
     *
     * subject  = email (uniquely identifies the user)
     * issuedAt = current timestamp
     * expiration = issuedAt + duration
     * signWith = HMAC-SHA256 using our secret key
     */
    private String buildToken(Map<String, Object> extraClaims,
                              UserDetails userDetails,
                              long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // ─────────────────────────────────────────
    // Token Validation
    // ─────────────────────────────────────────

    /**
     * Validate a token against a user.
     *
     * Two checks:
     *   1. The token's subject (email) matches the user
     *   2. The token has not expired
     *
     * The signature is verified automatically when we parse the token.
     * If the signature is invalid, parseSignedClaims() throws an exception.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()))
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ─────────────────────────────────────────
    // Claims Extraction
    // ─────────────────────────────────────────

    /**
     * Extract the email (subject) from the token.
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic claim extractor.
     *
     * Uses a Function to extract any claim from the token.
     * This avoids code duplication for each claim type.
     *
     * @param token          The JWT string
     * @param claimsResolver Function that extracts the desired claim
     */
    public <T> T extractClaim(String token,
                              Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse and return all claims from the token.
     *
     * This automatically verifies the signature.
     * If the signature is invalid or token is malformed,
     * JJWT throws an exception which propagates up.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Convert the secret key string to a cryptographic SecretKey.
     *
     * The secret must be at least 256 bits for HS256.
     * Our config value is a hex-encoded 256-bit key.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}