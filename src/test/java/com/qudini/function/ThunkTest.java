package com.qudini.function;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ThunkTest {

    @Test
    public void isRunOnlyOnce() {
        AtomicInteger evaluationCount = new AtomicInteger(0);

        Thunk<Integer> thunk = Thunk.of(evaluationCount::incrementAndGet);
        assertEquals(0, evaluationCount.get());
        thunk.get();
        assertEquals(1, evaluationCount.get());
        thunk.get();
        assertEquals(1, evaluationCount.get());
    }

    @Test
    public void computesCorrectValue() {
        Thunk<Integer> thunk = Thunk.of(() -> 42);
        assertEquals(42, (int) thunk.get());
        assertEquals(42, (int) thunk.get());
    }
}
