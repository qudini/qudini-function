package com.qudini.function;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Memoize function calls. Memoize procedures solely with side-effects, but do so with care. Setup max cache sizes and
 * time-based cache clearing intervals.
 */
@ParametersAreNonnullByDefault
public final class Memoizer {

    private static final int DEFAULT_MAX_CACHE_SIZE = 256;

    private Memoizer() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    private static <A, B> Function<A, B> memoize(
            int maxCacheSize,
            Optional<Duration> clearingInterval,
            Function<A, B> function
    ) {
        Map<A, B> previousResults = new HashMap<>();
        AtomicReference<LocalDateTime> lastClearTime = new AtomicReference<>(LocalDateTime.now());

        return x -> {
            final LocalDateTime now = LocalDateTime.now();
            final B result;

            synchronized (previousResults) {
                boolean durationExpired = clearingInterval
                        .map(interval -> lastClearTime.get().plus(interval).isBefore(now))
                        .orElse(false);
                if (durationExpired || (maxCacheSize <= (previousResults.size() + 1))) {
                    previousResults.clear();
                    lastClearTime.set(LocalDateTime.now());
                }
                if (previousResults.containsKey(x)) {
                    return previousResults.get(x);
                }
                result = function.apply(x);
                previousResults.put(x, result);
            }

            return result;
        };
    }

    /**
     * Return a memoized version of {@code function}. Cache {@code maxCacheSize} arguments before clearing. Clear the
     * cache regardless if {@code clearingInterval} has passed since the last clear.
     */
    @Nonnull
    public static <A, B> Function<A, B> memoize(int maxCacheSize, Duration clearingInterval, Function<A, B> function) {
        return memoize(maxCacheSize, Optional.of(clearingInterval), function);
    }

    /**
     * Return a memoized version of {@code function}. Cache {@code maxCacheSize} arguments before clearing.
     */
    @Nonnull
    public static <A, B> Function<A, B> memoize(int maxCacheSize, Function<A, B> function) {
        return memoize(maxCacheSize, Optional.empty(), function);
    }

    /**
     * Return a memoized version of {@code function}. Cache 256 arguments before clearing. Clear the cache regardless if
     * {@code clearingInterval} has passed since the last clear.
     */
    @Nonnull
    public static <A, B> Function<A, B> memoize(Duration clearingInterval, Function<A, B> function) {
        return memoize(DEFAULT_MAX_CACHE_SIZE, Optional.of(clearingInterval), function);
    }

    /**
     * Return a memoized version of {@code function}. Cache 256 arguments before clearing.
     */
    @Nonnull
    public static <A, B> Function<A, B> memoize(Function<A, B> function) {
        return memoize(DEFAULT_MAX_CACHE_SIZE, function);
    }

    @Nonnull
    private static <A> Supplier<A> memoize(Optional<Duration> clearingInterval, Supplier<A> thunk) {
        AtomicBoolean isSet = new AtomicBoolean(false);
        AtomicReference<A> previousResult = new AtomicReference<>();
        AtomicReference<LocalDateTime> lastClearTime = new AtomicReference<>(LocalDateTime.now());

        return () -> {
            final LocalDateTime now = LocalDateTime.now();
            final A result;

            synchronized (previousResult) {
                boolean durationExpired = clearingInterval
                        .map(interval -> lastClearTime.get().plus(interval).isBefore(now))
                        .orElse(false);
                if (durationExpired) {
                    isSet.set(false);
                    lastClearTime.set(LocalDateTime.now());
                }
                if (isSet.get()) {
                    return previousResult.get();
                }

                result = thunk.get();
                previousResult.set(result);
            }

            return result;
        };
    }

    /**
     * Return a memoized version of {@code supplier}. Clear the cached value if {@code clearingInterval} has passed
     * since the last clear.
     *
     * @see helpers.function.Thunk
     */
    @Nonnull
    public static <A> Supplier<A> memoize(Duration clearingInterval, Supplier<A> supplier) {
        return memoize(Optional.of(clearingInterval), supplier);
    }

    /**
     * Return a memoized version of {@code supplier}. Nothing more than a convenience wrapper around Thunk's
     * constructor.
     *
     * @see helpers.function.Thunk
     */
    @Nonnull
    public static <A> Supplier<A> memoize(Supplier<A> thunk) {
        return memoize(Optional.empty(), thunk);
    }
}
