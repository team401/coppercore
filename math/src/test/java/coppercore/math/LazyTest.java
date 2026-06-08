package coppercore.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

// This file was written with Codex 5.5.
class LazyTest {
    @Test
    void doesNotInitializeBeforeGet() {
        AtomicInteger initializerCalls = new AtomicInteger();

        new Lazy<>(
                () -> {
                    initializerCalls.incrementAndGet();
                    return "value";
                });

        assertEquals(0, initializerCalls.get());
    }

    @Test
    void getInitializesAndReturnsSupplierValue() {
        String value = "value";
        Lazy<String> lazy = new Lazy<>(() -> value);

        assertSame(value, lazy.get());
    }

    @Test
    void getCachesNonNullValue() {
        AtomicInteger initializerCalls = new AtomicInteger();
        Lazy<Object> lazy =
                new Lazy<>(
                        () -> {
                            initializerCalls.incrementAndGet();
                            return new Object();
                        });

        Object firstValue = lazy.get();
        Object secondValue = lazy.get();

        assertSame(firstValue, secondValue);
        assertEquals(1, initializerCalls.get());
    }

    @Test
    void nullSupplierValueThrowsAndIsNotCached() {
        AtomicInteger initializerCalls = new AtomicInteger();
        Lazy<Object> lazy =
                new Lazy<>(
                        () -> {
                            initializerCalls.incrementAndGet();
                            return null;
                        });

        assertThrows(NullPointerException.class, lazy::get);
        assertThrows(NullPointerException.class, lazy::get);

        assertEquals(2, initializerCalls.get());
    }

    @Test
    void initializerExceptionDoesNotCacheFailure() {
        AtomicInteger initializerCalls = new AtomicInteger();
        Lazy<String> lazy =
                new Lazy<>(
                        () -> {
                            if (initializerCalls.incrementAndGet() == 1) {
                                throw new IllegalStateException("not ready");
                            }
                            return "value";
                        });

        assertThrows(IllegalStateException.class, lazy::get);

        assertEquals("value", lazy.get());
        assertEquals(2, initializerCalls.get());
    }
}
