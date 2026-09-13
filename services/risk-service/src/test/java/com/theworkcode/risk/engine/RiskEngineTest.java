package com.theworkcode.risk.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.theworkcode.risk.engine.RiskEngine.Assessment;

class RiskEngineTest {

    @Test
    void cleanVerificationIsLowRisk() {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("identityResult", "MATCH");
        ctx.put("identityConfidence", 98.6);
        ctx.put("employmentResult", "VERIFIED");
        ctx.put("incomeResult", "VERIFIED");

        Assessment a = RiskEngine.analyze(ctx);
        assertTrue(a.score() <= 29, "expected LOW, got " + a.score());
        assertEquals("LOW", a.band());
        assertTrue(a.negatives().isEmpty());
        assertTrue(a.positives().size() >= 4);
    }

    @Test
    void duplicateIdentityIsHighRisk() {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("identityResult", "MATCH");
        ctx.put("duplicateSuspected", true);
        ctx.put("highVerificationVelocity", true);
        ctx.put("suspiciousRequestPattern", true);

        Assessment a = RiskEngine.analyze(ctx);
        assertTrue(a.score() >= 60, "expected HIGH+ got " + a.score());
        assertTrue(a.negatives().stream().anyMatch(s -> s.code().equals("DUPLICATE_IDENTITY")));
    }

    @Test
    void incomeAnomalyMatchesThresholdRule() {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("identityResult", "MATCH");
        ctx.put("incomeVariance", 0.62);

        Assessment a = RiskEngine.analyze(ctx);
        assertTrue(a.negatives().stream().anyMatch(s -> s.code().equals("INCOME_ANOMALY")));
        assertTrue(a.score() > 0);
    }

    @Test
    void employerMismatchAddsEmploymentRisk() {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("identityResult", "MATCH");
        ctx.put("employerMismatch", true);

        Assessment a = RiskEngine.analyze(ctx);
        assertTrue(a.score() >= 20, "employer mismatch severity 4/5 * 25 weight should add >=20");
        assertEquals("MEDIUM", a.band());
    }

    @Test
    void repeatedFailuresEscalate() {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("identityResult", "NO_MATCH");
        ctx.put("failedAttempts", 7);

        Assessment a = RiskEngine.analyze(ctx);
        assertTrue(a.score() >= 40);
        assertTrue(a.negatives().stream().anyMatch(s -> s.code().equals("REPEATED_FAILED_IDENTITY")));
    }

    @Test
    void bandsMapCorrectly() {
        assertEquals("LOW", bandOf(Map.of("identityResult", "MATCH", "employmentResult", "VERIFIED",
                "incomeResult", "VERIFIED")));
        assertEquals("MEDIUM", bandOf(Map.of("identityResult", "MATCH", "employerMismatch", true)));
        assertEquals("HIGH", bandOf(Map.of("identityResult", "MATCH", "duplicateSuspected", true,
                "highVerificationVelocity", true, "suspiciousRequestPattern", true)));
        assertEquals("CRITICAL", bandOf(allNegative()));
    }

    private String bandOf(Map<String, Object> ctx) {
        return RiskEngine.analyze(ctx).band();
    }

    private Map<String, Object> allNegative() {
        Map<String, Object> ctx = new java.util.HashMap<>();
        ctx.put("identityResult", "NO_MATCH");
        ctx.put("employmentResult", "NOT_VERIFIED");
        ctx.put("incomeResult", "REVIEW");
        ctx.put("failedAttempts", 20);
        ctx.put("duplicateSuspected", true);
        ctx.put("highVerificationVelocity", true);
        ctx.put("suspiciousRequestPattern", true);
        ctx.put("employerFlagged", true);
        ctx.put("impossiblePayroll", true);
        ctx.put("overlappingTenures", true);
        ctx.put("sharedEmail", true);
        ctx.put("recentDuplicateRequest", true);
        return ctx;
    }

    @Test
    void allNegativeContextSaturatesAt100() {
        Assessment a = RiskEngine.analyze(allNegative());
        assertEquals(100, a.score());
    }

    @Test
    void scoringIsDeterministic() {
        Map<String, Object> ctx = Map.of(
                "identityResult", "MATCH",
                "incomeVariance", 0.5,
                "duplicateSuspected", true);
        int s1 = RiskEngine.analyze(ctx).score();
        int s2 = RiskEngine.analyze(ctx).score();
        assertEquals(s1, s2);
    }
}
