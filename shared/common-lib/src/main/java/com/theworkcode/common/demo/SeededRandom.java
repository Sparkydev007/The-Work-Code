package com.theworkcode.common.demo;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic pseudo-random source for demo data generation. Same seed
 * always produces the same dataset on every machine - required by the
 * "deterministic seed" requirement.
 */
public final class SeededRandom {

    private long state;

    public SeededRandom(long seed) {
        this.state = seed;
    }

    /** SplitMix64 next value. */
    public long nextLong() {
        state += 0x9E3779B97F4A7C15L;
        long z = state;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive");
        }
        return (int) Long.remainderUnsigned(nextLong(), bound);
    }

    /** Uniform integer in [min, max] inclusive. */
    public int between(int min, int max) {
        return min + nextInt(max - min + 1);
    }

    public long between(long min, long max) {
        return min + Long.remainderUnsigned(nextLong(), max - min + 1);
    }

    public boolean chance(int percent) {
        return nextInt(100) < percent;
    }

    public <T> T pick(List<T> items) {
        return items.get(nextInt(items.size()));
    }

    @SafeVarargs
    public final <T> T pick(T... items) {
        return items[nextInt(items.length)];
    }

    /** Fisher-Yates shuffle of a copy. */
    public <T> List<T> shuffle(List<T> input) {
        List<T> copy = new ArrayList<>(input);
        for (int i = copy.size() - 1; i > 0; i--) {
            int j = nextInt(i + 1);
            T tmp = copy.get(i);
            copy.set(i, copy.get(j));
            copy.set(j, tmp);
        }
        return copy;
    }
}
