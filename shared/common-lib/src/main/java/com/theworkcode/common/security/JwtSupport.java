package com.theworkcode.common.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Minimal JWT issuer/validator used by the demo auth flow. Tokens are HS256
 * signed with {@code JWT_SECRET}; claims carry subject, role and organization.
 *
 * Demo-only: production deployments should use short-lived access tokens with
 * refresh tokens and a key rotation strategy.
 */
public final class JwtSupport {

    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_ORG = "org";
    public static final String CLAIM_NAME = "name";

    private final SecretKey key;
    private final long ttlSeconds;

    public JwtSupport(String secret, long ttlSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlSeconds = ttlSeconds;
    }

    public String issue(String subject, String role, String orgId, String name) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .claims(Map.of(
                        CLAIM_ROLE, role,
                        CLAIM_ORG, orgId,
                        CLAIM_NAME, name))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(key)
                .compact();
    }

    /** Returns claims if the token is valid and unexpired, else null. */
    public Claims parse(String token) {
        try {
            return Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public long ttlSeconds() {
        return ttlSeconds;
    }
}
