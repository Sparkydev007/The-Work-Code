package com.theworkcode.identity.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Transparent deterministic string similarity used by the identity engine.
 * Combines character-bigram Dice coefficient with word-token Jaccard —
 * robust to typos, reordering and spacing variants.
 *
 * Not derived from any commercial verification provider's algorithm.
 */
public final class TextSimilarity {

    private TextSimilarity() {
    }

    /** Normalize: lowercase, strip punctuation (incl. dots), collapse whitespace. */
    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        return input.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9@\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /** Character bigrams of the normalized string (ignoring spaces). */
    private static List<String> bigrams(String normalized) {
        String squeezed = normalized.replace(" ", "");
        List<String> grams = new ArrayList<>();
        for (int i = 0; i < squeezed.length() - 1; i++) {
            grams.add(squeezed.substring(i, i + 2));
        }
        return grams;
    }

    /** Dice coefficient over character bigrams: 2|A∩B| / (|A|+|B|). */
    public static double bigramDice(String a, String b) {
        List<String> ga = bigrams(normalize(a));
        List<String> gb = bigrams(normalize(b));
        if (ga.isEmpty() || gb.isEmpty()) {
            return 0.0;
        }
        Set<String> setB = new HashSet<>(gb);
        int overlap = 0;
        Set<String> seenA = new HashSet<>();
        for (String g : ga) {
            if (setB.contains(g) && seenA.add(g)) {
                overlap++;
            }
        }
        return 2.0 * overlap / (new HashSet<>(ga).size() + new HashSet<>(gb).size());
    }

    /** Jaccard similarity over normalized word tokens: |A∩B| / |A∪B|. */
    public static double tokenSimilarity(String a, String b) {
        String na = normalize(a);
        String nb = normalize(b);
        if (na.isEmpty() || nb.isEmpty()) {
            return 0.0;
        }
        String[] ta = na.split(" ");
        String[] tb = nb.split(" ");
        Set<String> setA = new HashSet<>(List.of(ta));
        Set<String> setB = new HashSet<>(List.of(tb));
        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);
        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);
        return (double) intersection.size() / union.size();
    }

    /** Levenshtein edit distance on normalized strings. */
    public static int levenshtein(String a, String b) {
        String na = normalize(a);
        String nb = normalize(b);
        int[][] dp = new int[na.length() + 1][nb.length() + 1];
        for (int i = 1; i <= na.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 1; j <= nb.length(); j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= na.length(); i++) {
            for (int j = 1; j <= nb.length(); j++) {
                int cost = na.charAt(i - 1) == nb.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[na.length()][nb.length()];
    }

    /** 1.0 - distance/maxLen, bounded to [0,1]. */
    public static double editSimilarity(String a, String b) {
        String na = normalize(a).replace(" ", "");
        String nb = normalize(b).replace(" ", "");
        if (na.isEmpty() && nb.isEmpty()) {
            return 1.0;
        }
        if (na.isEmpty() || nb.isEmpty()) {
            return 0.0;
        }
        int maxLen = Math.max(na.length(), nb.length());
        return 1.0 - (double) levenshtein(a, b) / maxLen;
    }

    /**
     * Weighted blend of the three signals. The max() guard keeps strong
     * bigram agreement from being diluted by token reordering.
     */
    public static double blended(String a, String b) {
        double dice = bigramDice(a, b);
        double token = tokenSimilarity(a, b);
        double edit = editSimilarity(a, b);
        return Math.max(dice, 0.45 * dice + 0.25 * token + 0.30 * edit);
    }
}
