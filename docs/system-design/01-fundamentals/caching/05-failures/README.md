---
sidebar_label: "Failure Modes"
---

# Failure Modes & Anti-patterns

## Concept

This concept explores what breaks in caching systems, common anti-patterns, and how to design resilient caching that degrades gracefully under failure.

## Why This Exists

Caching failures can be catastrophic - stale data, cache stampedes, memory leaks, and cascading failures. Understanding failure modes is essential for production systems.

**What problem does this solve?**
- Prevents cache-related outages
- Enables graceful degradation
- Helps debug production issues

**Why does this exist?**
Caching is a double-edged sword - it can make or break system reliability. Failure to understand these modes leads to fragile systems.

## Mental Model

```
Cache Failure Scenarios:
🐛 Cache Poisoning    💥 Cache Stampede
🕰️  Clock Skew        🔥 Hot Key Problem
💾 Memory Exhaustion  🤖 Thundering Herd
```

## Code Walkthrough

Demonstrates failure scenarios and defensive patterns like circuit breakers, cache warming, and graceful degradation.

### Java Implementation
[View Java code](java/src/ResilientCache.java) | [View tests](java/test/ResilientCacheTest.java)

```java
public class ResilientCache<K, V> {
    private final Map<K, V> storage = new HashMap<>();
    private final int maxFailures;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private volatile boolean circuitOpen = false;

    // Circuit breaker opens after maxFailures
    public V getWithCircuitBreaker(K key, Supplier<V> fallback) {
        if (circuitOpen) {
            return fallback.get(); // Graceful degradation
        }
        // ... implementation with failure tracking
    }

    // Cache warming prevents cold starts
    public void warm(Supplier<Map<K, V>> dataLoader) {
        try {
            Map<K, V> warmData = dataLoader.get();
            storage.putAll(warmData);
        } catch (Exception e) {
            // Log but don't fail - cache still works
        }
    }
}
```

### Python Implementation
[View Python code](python/src/resilient_cache.py) | [View tests](python/tests/test_resilient_cache.py)

```python
class ResilientCache(Generic[K, V]):
    def __init__(self, max_failures: int = 3):
        self._circuit_open = False
        self._failure_count = 0

    def get_with_circuit_breaker(self, key: K, fallback: Callable[[], V]) -> V:
        """Circuit breaker with graceful degradation"""
        if self._circuit_open:
            return fallback()  # Graceful degradation

        try:
            result = self._storage.get(key)
            if result is not None:
                self._failure_count = 0  # Reset on success
                return result
            return fallback()
        except Exception as e:
            self._failure_count += 1
            if self._failure_count >= self._max_failures:
                self._circuit_open = True
            raise e

    def warm(self, data_loader: Callable[[], Dict[K, V]]) -> None:
        """Cache warming"""
        try:
            warm_data = data_loader()
            self._storage.update(warm_data)
        except Exception as e:
            print(f"Cache warming failed: {e}")  # Log but don't fail

## Tests & What They Prove

### Circuit Breaker Activation
```java
// Simulate 3 backend failures
for (int i = 0; i < 3; i++) {
    cache.getWithCircuitBreaker("key", () -> { throw new RuntimeException("Backend down"); });
}
// Test: cache.isCircuitOpen() == true
```
**Proves:** Circuit breaker prevents cascade failures by opening after threshold failures.

### Graceful Degradation
```java
// When circuit is open, use fallback
String result = cache.getWithCircuitBreaker("key", () -> "fallback-data");
// Test: result.equals("fallback-data")
```
**Proves:** System continues working with degraded functionality instead of failing completely.

### Cache Warming Recovery
```java
// Warming fails but cache still works
cache.warm(() -> { throw new RuntimeException("Loader failed"); });
cache.put("key", "value");
// Test: cache.get("key") == "value"
```
**Proves:** Cache warming failures don't break the cache - it continues serving requests.

### Failure Count Reset
```java
// After success, failure count resets
cache.getWithCircuitBreaker("existing-key", () -> "fallback");
// Test: cache.getFailureCount() == 0
```
**Proves:** Circuit breaker recovers automatically after successful operations.

## Common Misconceptions

❌ **"Circuit breakers make systems slower"**
- Reality: They prevent total failure, allowing graceful degradation

❌ **"Cache warming is always necessary"**
- Reality: Only needed for predictable, expensive operations; adds startup time

❌ **"Once circuit opens, system is broken"**
- Reality: Circuit allows fallbacks; can auto-reset after success

❌ **"Failure handling adds complexity I don't need"**
- Reality: Without it, single failures cascade into system-wide outages

❌ **"Tests don't need to cover failure scenarios"**
- Reality: Failure modes are where systems break - test them rigorously

