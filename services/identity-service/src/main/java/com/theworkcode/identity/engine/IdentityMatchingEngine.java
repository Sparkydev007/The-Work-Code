package com.theworkcode.identity.engine;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.theworkcode.common.demo.DemoDataFactory;

/**
 * Deterministic identity matching engine.
 *
 * <p>Weights (documented, transparent, original - not derived from any
 * commercial provider's proprietary algorithm):
 *
 * <pre>
 *   Name similarity     40%
 *   Employer match      25%
 *   Employee ID          25%
 *   Email/domain match   10%
 * </pre>
 *
 * <p>Outcomes:
 * <pre>
 *   >= 90  -> MATCH
 *   60-89  -> NO_MATCH (review zone; callers may route to manual review)
 *   < 60   -> NO_MATCH
 *   no candidate record found -> NO_HIT
 * </pre>
 */
public final class IdentityMatchingEngine {

    public static final double W_NAME = 0.40;
    public static final double W_EMPLOYER = 0.25;
    public static final double W_EMPLOYEE_ID = 0.25;
    public static final double W_EMAIL = 0.10;

    public static final int THRESHOLD_MATCH = 90;
    public static final int THRESHOLD_REVIEW = 60;

    public record Candidate(String employeeNumber, String name, String employerName, String email) {
    }

    /** Attribute scores for one comparison. */
    public record AttributeScores(double name, double employer, double employeeId, double email) {

        public double weighted() {
            return 100.0 * (W_NAME * name + W_EMPLOYER * employer + W_EMPLOYEE_ID * employeeId + W_EMAIL * email);
        }
    }

    public record MatchOutcome(String result, double confidence, AttributeScores scores,
            String matchedEmployeeNumber, Map<String, Object> breakdown) {
    }

    private IdentityMatchingEngine() {
    }

    /**
     * Match input attributes against the synthetic employee index. When the
     * index contains no plausible candidate (no employee-ID hit and no
     * name+employer overlap) the result is NO_HIT.
     */
    public static MatchOutcome verify(String inputName, String inputEmployeeNumber, String inputEmail,
            String inputEmployer, List<Candidate> candidates) {

        if (candidates == null || candidates.isEmpty()) {
            return new MatchOutcome("NO_HIT", 0.0, new AttributeScores(0, 0, 0, 0), null,
                    Map.of("reason", "No synthetic record found for the supplied identifiers."));
        }

        // Pick the best candidate by weighted score.
        AttributeScores bestScores = null;
        Candidate bestCandidate = null;
        double bestWeighted = -1;
        for (Candidate c : candidates) {
            AttributeScores s = score(inputName, inputEmployeeNumber, inputEmail, inputEmployer, c);
            double w = s.weighted();
            if (w > bestWeighted) {
                bestWeighted = w;
                bestCandidate = c;
                bestScores = s;
            }
        }

        Map<String, Object> breakdown = new LinkedHashMap<>();
        breakdown.put("weights", Map.of(
                "name", W_NAME,
                "employer", W_EMPLOYER,
                "employeeId", W_EMPLOYEE_ID,
                "email", W_EMAIL));
        breakdown.put("algorithm", "normalize -> token/edit similarity -> weighted score (original demo algorithm)");
        breakdown.put("matchedEmployeeNumber", bestCandidate.employeeNumber());

        String result = bestWeighted >= THRESHOLD_MATCH ? "MATCH"
                : (bestWeighted >= THRESHOLD_REVIEW ? "NO_MATCH" : "NO_MATCH");

        double confidence = Math.round(bestWeighted * 10.0) / 10.0;
        return new MatchOutcome(result, confidence, bestScores, bestCandidate.employeeNumber(), breakdown);
    }

    private static AttributeScores score(String inputName, String inputEmployeeNumber, String inputEmail,
            String inputEmployer, Candidate c) {
        double nameScore;
        if (isBlank(inputName) || isBlank(c.name())) {
            nameScore = 0.0;
        } else {
            nameScore = Math.min(1.0, TextSimilarity.blended(inputName, c.name()) + exactBonus(inputName, c.name()));
        }

        double employerScore;
        if (isBlank(inputEmployer) || isBlank(c.employerName())) {
            employerScore = 0.5; // neutral when the field is not supplied
        } else {
            employerScore = TextSimilarity.blended(inputEmployer, c.employerName());
        }

        double employeeIdScore;
        if (isBlank(inputEmployeeNumber) || isBlank(c.employeeNumber())) {
            employeeIdScore = 0.5; // neutral
        } else {
            employeeIdScore = normalizeId(inputEmployeeNumber).equals(normalizeId(c.employeeNumber())) ? 1.0 : 0.0;
        }

        double emailScore;
        if (isBlank(inputEmail) || isBlank(c.email())) {
            emailScore = 0.5; // neutral
        } else {
            boolean exact = normalizeEmail(inputEmail).equals(normalizeEmail(c.email()));
            boolean sameDomain = domainOf(inputEmail).equalsIgnoreCase(domainOf(c.email()));
            emailScore = exact ? 1.0 : sameDomain ? 0.7 : 0.0;
        }

        return new AttributeScores(nameScore, employerScore, employeeIdScore, emailScore);
    }

    /** Exact (normalized) name equality scores slightly above fuzzy similarity. */
    private static double exactBonus(String a, String b) {
        return TextSimilarity.normalize(a).equals(TextSimilarity.normalize(b)) ? 0.05 : 0.0;
    }

    private static String normalizeId(String id) {
        return TextSimilarity.normalize(id).replace(" ", "");
    }

    private static String normalizeEmail(String email) {
        return TextSimilarity.normalize(email).replace(" ", "").toLowerCase(Locale.ROOT);
    }

    private static String domainOf(String email) {
        int at = email.indexOf('@');
        return at < 0 ? "" : email.substring(at + 1).toLowerCase(Locale.ROOT);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /** Builds the candidate list used by the demo provider (scenario employees + lookup). */
    public static List<Candidate> candidateFromEmployee(String employeeNumber, String name, String employerName,
            String email) {
        return List.of(new Candidate(employeeNumber, name, employerName, email));
    }

    /** Utility used by tests and demo tooling. */
    public static Candidate scenarioCandidate(String scenarioNumber) {
        var employees = DemoDataFactory.employees(200);
        return employees.stream()
                .filter(e -> e.employeeNumber().equals(scenarioNumber))
                .findFirst()
                .map(e -> new Candidate(e.employeeNumber(), e.name(), e.employerName(), e.email()))
                .orElse(null);
    }
}
