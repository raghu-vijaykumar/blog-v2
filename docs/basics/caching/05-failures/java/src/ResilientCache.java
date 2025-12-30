import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ResilientCache<K, V> {
    private final Map<K, V> storage = new HashMap<>();
    private final int maxFailures;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private volatile boolean circuitOpen = false;

    public ResilientCache(int maxFailures) {
        this.maxFailures = maxFailures;
    }

    public V get(K key) {
        if (circuitOpen) {
            throw new RuntimeException("Circuit breaker open - cache unavailable");
        }
        return storage.get(key);
    }

    public void put(K key, V value) {
        if (circuitOpen) {
            throw new RuntimeException("Circuit breaker open - cache unavailable");
        }
        storage.put(key, value);
    }

    // Circuit breaker pattern - open circuit after too many failures
    public V getWithCircuitBreaker(K key, Supplier<V> fallback) {
        if (circuitOpen) {
            return fallback.get(); // Graceful degradation
        }

        try {
            V result = storage.get(key);
            if (result != null) {
                return result; // Success - reset failure count
            }
            failureCount.set(0);
            return fallback.get();
        } catch (Exception e) {
            if (failureCount.incrementAndGet() >= maxFailures) {
                circuitOpen = true;
            }
            throw e;
        }
    }

    // Cache warming - pre-populate cache to avoid cold starts
    public void warm(Supplier<Map<K, V>> dataLoader) {
        try {
            Map<K, V> warmData = dataLoader.get();
            storage.putAll(warmData);
        } catch (Exception e) {
            // Log warning but don't fail - cache can still serve requests
            System.err.println("Cache warming failed: " + e.getMessage());
        }
    }

    // Reset circuit breaker (for testing/admin purposes)
    public void reset() {
        circuitOpen = false;
        failureCount.set(0);
    }

    public boolean isCircuitOpen() {
        return circuitOpen;
    }

    public int getFailureCount() {
        return failureCount.get();
    }
}
