package com.qudini.function;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Represents a single value whose computation has been delayed until it's actually necessary. Useful for lazy value
 * definitions. Only a single computation occurs; all access afterwards use the cached value.
 */
@ParametersAreNonnullByDefault
public final class Thunk<A> {
    
    private final Supplier<A> compute;
    private Optional<A> maybeComputed = Optional.empty();

    private Thunk(Supplier<A> compute) {
        this.compute = compute;
    }

    @Nonnull
    public synchronized A get() {
        return maybeComputed.orElseGet(() -> {
            A computed = compute.get();
            maybeComputed = Optional.of(computed);
            return computed;
        });
    }
    
    @CheckReturnValue
    @Nonnull
    public static <T> Thunk<T> of(Supplier<T> compute) {
        return new Thunk<>(compute);
    }
    
}
