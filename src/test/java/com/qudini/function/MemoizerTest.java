package com.qudini.function;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static java.util.stream.IntStream.rangeClosed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class MemoizerTest {

    private static final Function<Integer, Integer> f = x -> x * x;

    @Test
    public void thunks() {
        assertEquals(42, (long) Memoizer.memoize(() -> 42).get());
    }

    @Test
    public void memoizes() {
        int result = 42 * 42;
        Function<Integer, Integer> g = Memoizer.memoize(f);

        assertEquals(result, (int) f.apply(42));
        assertEquals(result, (int) g.apply(42));
        assertEquals(result, (int) g.apply(42));
    }

    @Test
    public void overfilledCacheClears() {
        int cacheSize = 32;
        AtomicBoolean previousComputationWasZero = new AtomicBoolean(false);

        Function<Integer, Integer> g = Memoizer.memoize(cacheSize, x -> {
            previousComputationWasZero.set(x == 0);
            return 42;
        });

        assertFalse(previousComputationWasZero.get());
        g.apply(0);
        assertTrue(previousComputationWasZero.get());
        g.apply(1);
        assertFalse(previousComputationWasZero.get());
        assertFalse(previousComputationWasZero.get());

        // Exhaust the cache, forcing computed values again.
        rangeClosed(1, cacheSize).forEach(g::apply);

        assertFalse(previousComputationWasZero.get());
        g.apply(0);
        assertTrue(previousComputationWasZero.get());
    }

    @Test
    public void cacheClearsAfterTimeInterval() {
        AtomicBoolean previousComputationWasZero = new AtomicBoolean(false);
        int intervalDuration = 2;

        Function<Integer, Integer> g = Memoizer.memoize(Duration.ofSeconds(intervalDuration), x -> {
            previousComputationWasZero.set(x == 0);
            return 42;
        });

        assertFalse(previousComputationWasZero.get());
        g.apply(0);
        assertTrue(previousComputationWasZero.get());
        g.apply(1);
        assertFalse(previousComputationWasZero.get());
        assertFalse(previousComputationWasZero.get());

        // Wait beyond the cache clearing interval, forcing computed values again.
        try {
            TimeUnit.SECONDS.sleep(intervalDuration + 3);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertFalse(previousComputationWasZero.get());
        g.apply(0);
        assertTrue(previousComputationWasZero.get());
    }
}
