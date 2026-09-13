package com.theworkcode.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JwtSupportTest {

    private final JwtSupport jwt = new JwtSupport("demo-secret-key-change-me-0123456789abcdef", 3600);

    @Test
    void issueAndParseRoundTrip() {
        String token = jwt.issue("user-123", Role.ANALYST.name(), "ORG-8821", "Alex Morgan");
        var claims = jwt.parse(token);
        assertNotNull(claims);
        assertEquals("user-123", claims.getSubject());
        assertEquals("ANALYST", claims.get(JwtSupport.CLAIM_ROLE, String.class));
        assertEquals("ORG-8821", claims.get(JwtSupport.CLAIM_ORG, String.class));
        assertEquals("Alex Morgan", claims.get(JwtSupport.CLAIM_NAME, String.class));
    }

    @Test
    void rejectsTamperedToken() {
        String token = jwt.issue("user-123", Role.ADMIN.name(), "ORG-8821", "Alex");
        String tampered = token.substring(0, token.length() - 4) + "AAAA";
        assertNull(jwt.parse(tampered));
    }

    @Test
    void rejectsGarbage() {
        assertNull(jwt.parse("not-a-token"));
        assertNull(jwt.parse(""));
    }

    @Test
    void differentSecretRejectsToken() {
        JwtSupport other = new JwtSupport("another-secret-key-0123456789abcdef", 3600);
        String token = jwt.issue("user-123", Role.VIEWER.name(), "ORG-1", "V");
        assertNull(other.parse(token));
    }

    @Test
    void roleCapabilityChecks() {
        assertTrue(Role.ADMIN.has("anything"));
        assertTrue(Role.ANALYST.has("verification"));
        assertTrue(Role.DEVELOPER.has("api-keys"));
        assertTrue(Role.VIEWER.has("reports:read"));
    }
}
