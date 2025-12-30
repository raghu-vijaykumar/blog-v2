import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

class ResilientCacheTest {

    private ResilientCache<String, String> cache;

    @BeforeEach
    void setUp() {
        cache = new ResilientCache<>(3); // Circuit opens after 3 failures
    }

    @Test
    void testNormalOperation() {
        cache.put("key1", "value1");
        assertEquals("value1", cache.get("key1"));
        assertFalse(cache.isCircuitOpen());
    }

    @Test
    void testCircuitBreakerOpensAfterFailures() {
        // Simulate failures
        assertThrows(RuntimeException.class, () -> {
            cache.getWithCircuitBreaker("missing", () -> {
                throw new RuntimeException("Backend failure");
            });
        });

        assertThrows(RuntimeException.class, () -> {
            cache.getWithCircuitBreaker("missing", () -> {
                throw new RuntimeException("Backend failure");
            });
        });

        // Third failure should open circuit
        assertThrows(RuntimeException.class, () -> {
            cache.getWithCircuitBreaker("missing", () -> {
                throw new RuntimeException("Backend failure");
            });
        });

        assertTrue(cache.isCircuitOpen());
    }

    @Test
    void testGracefulDegradationWhenCircuitOpen() {
        // Open the circuit
        for (int i = 0; i < 3; i++) {
            try {
                cache.getWithCircuitBreaker("missing", () -> {
                    throw new RuntimeException("Backend failure");
                });
            } catch (RuntimeException e) {
                // Expected
            }
        }

        // Now circuit should be open, fallback should be used
        String result = cache.getWithCircuitBreaker("missing", () -> "fallback-value");
        assertEquals("fallback-value", result);
    }

    @Test
    void testCacheWarming() {
        Map<String, String> warmData = new HashMap<>();
        warmData.put("warm-key", "warm-value");

        cache.warm(() -> warmData);

        assertEquals("warm-value", cache.get("warm-key"));
    }

    @Test
    void testCacheWarmingFailure() {
        // Warming fails but cache should still work
        cache.warm(() -> {
            throw new RuntimeException("Data loader failed");
        });

        // Cache should still accept new puts
        cache.put("key", "value");
        assertEquals("value", cache.get("key"));
    }

    @Test
    void testCircuitBreakerReset() {
        // Open circuit
        for (int i = 0; i < 3; i++) {
            try {
                cache.getWithCircuitBreaker("missing", () -> {
                    throw new RuntimeException("Backend failure");
                });
            } catch (RuntimeException e) {
                // Expected
            }
        }

        assertTrue(cache.isCircuitOpen());

        // Reset
        cache.reset();
        assertFalse(cache.isCircuitOpen());

        // Should work again
        cache.put("key", "value");
        assertEquals("value", cache.get("key"));
    }

    @Test
    void testFailureCountTracking() {
        assertEquals(0, cache.getFailureCount());

        try {
            cache.getWithCircuitBreaker("missing", () -> {
                throw new RuntimeException("Backend failure");
            });
        } catch (RuntimeException e) {
            // Expected
        }

        assertEquals(1, cache.getFailureCount());
    }
}
