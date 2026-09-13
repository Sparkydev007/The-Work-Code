package com.theworkcode.identity.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.theworkcode.identity.engine.IdentityMatchingEngine.MatchOutcome;

import org.junit.jupiter.api.Test;

class IdentityMatchingEngineTest {

    private static final IdentityMatchingEngine.Candidate PERFECT = new IdentityMatchingEngine.Candidate(
            "DEMO-PERFECT-001", "Rahul Sharma", "Acme Technologies Pvt Ltd", "rahul.sharma@acme-demo.com");

    @Test
    void exactMatchScoresNearPerfect() {
        MatchOutcome outcome = IdentityMatchingEngine.verify(
                "Rahul Sharma", "DEMO-PERFECT-001", "rahul.sharma@acme-demo.com",
                "Acme Technologies Pvt Ltd", List.of(PERFECT));
        assertEquals("MATCH", outcome.result());
        assertTrue(outcome.confidence() >= 90, "expected >=90, got " + outcome.confidence());
        assertEquals("DEMO-PERFECT-001", outcome.matchedEmployeeNumber());
    }

    @Test
    void nameTypoStillMatches() {
        MatchOutcome outcome = IdentityMatchingEngine.verify(
                "Rahul Sharman", "DEMO-PERFECT-001", "rahul.sharma@acme-demo.com",
                "Acme Technologies Pvt Ltd", List.of(PERFECT));
        assertEquals("MATCH", outcome.result());
    }

    @Test
    void wrongNameIsNoMatch() {
        MatchOutcome outcome = IdentityMatchingEngine.verify(
                "Vikram Desai", "DEMO-PERFECT-001", "rahul.sharma@acme-demo.com",
                "Acme Technologies Pvt Ltd", List.of(PERFECT));
        // name≈0, employer=1, id=1, email=1 -> 0.25+0.25+0.10 = 0.60 -> 60 -> review zone -> NO_MATCH
        assertEquals("NO_MATCH", outcome.result());
        assertTrue(outcome.confidence() < 90);
    }

    @Test
    void wrongEmployeeIdDropsBelowMatch() {
        MatchOutcome outcome = IdentityMatchingEngine.verify(
                "Rahul Sharma", "EMP-00000", "rahul.sharma@acme-demo.com",
                "Acme Technologies Pvt Ltd", List.of(PERFECT));
        // name=1, employer=1, id=0, email=1 -> 0.40+0.25+0.10 = 0.75 -> NO_MATCH
        assertEquals("NO_MATCH", outcome.result());
    }

    @Test
    void unknownRecordIsNoHit() {
        MatchOutcome outcome = IdentityMatchingEngine.verify(
                "Nobody Person", null, null, null, List.of());
        assertEquals("NO_HIT", outcome.result());
        assertEquals(0.0, outcome.confidence());
    }

    @Test
    void partialAttributesUseNeutralScores() {
        // Only name + employer supplied and they match exactly.
        MatchOutcome outcome = IdentityMatchingEngine.verify(
                "Rahul Sharma", null, null, "Acme Technologies Pvt Ltd", List.of(PERFECT));
        // name≈1(+bonus), employer=1, id=0.5, email=0.5 -> 0.40+0.25+0.125+0.05 = ~0.825 -> NO_MATCH zone
        assertNotNull(outcome.scores());
        assertTrue(outcome.confidence() > 60, "expected review-zone score, got " + outcome.confidence());
        assertEquals("NO_MATCH", outcome.result());
    }

    @Test
    void similarityIsDeterministic() {
        double a = TextSimilarity.blended("Rahul Sharma", "Rahul Sharman");
        double b = TextSimilarity.blended("Rahul Sharma", "Rahul Sharman");
        assertEquals(a, b);
        assertTrue(a > 0.8);
    }

    @Test
    void normalizationHandlesCaseAndPunctuation() {
        assertEquals("acme technologies pvt ltd", TextSimilarity.normalize("  ACME  Technologies, Pvt. Ltd! "));
        assertTrue(TextSimilarity.blended("O'Brien-Smith", "O Brien Smith") > 0.9);
    }
}
