# 01 – Cache Basics

## Concept

Building on the mental model, this concept explores the fundamental cache operations that form the basis of all caching systems: cache hits, cache misses, and the cache-aside pattern.

## Why This Exists

While the mental model shows *what* caching is, the basics show *how* caching works in practice. These operations are the building blocks for all caching implementations.

**What problem does this solve?**
- Establishes the standard cache interface used across all systems
- Defines the behavior expectations for cache operations
- Provides the foundation for more complex caching patterns

**Why does this exist?**
Without these basics, every caching implementation would be inconsistent and unpredictable.

## Mental Model

```
Request Flow:
User → Cache → Backend
       ↓
   Hit: Return
   Miss: Fetch → Store → Return
```

## Code Walkthrough

This concept implements a basic cache with hit/miss tracking and demonstrates the cache-aside pattern - the most common way to integrate caching with data sources.

### Java Implementation
[View Java code](java/src/BasicCache.java) | [View tests](java/test/BasicCacheTest.java)

```java
public class BasicCache<K, V> {
    private final Map<K, V> storage = new HashMap<>();
    private int hits = 0;
    private int misses = 0;

    public V get(K key) {
        V value = storage.get(key);
        if (value != null) {
            hits++;
            return value;
        }
        misses++;
        return null;
    }

    public void put(K key, V value) {
        storage.put(key, value);
    }

    // Statistics for monitoring
    public int getHits() { return hits; }
    public int getMisses() { return misses; }
    public double getHitRate() {
        int total = hits + misses;
        return total == 0 ? 0.0 : (double) hits / total;
    }
}
```

### Python Implementation
[View Python code](python/src/basic_cache.py) | [View tests](python/tests/test_basic_cache.py)

```python
class BasicCache:
    def __init__(self):
        self._storage = {}
        self._hits = 0
        self._misses = 0

    def get(self, key):
        if key in self._storage:
            self._hits += 1
            return self._storage[key]
        self._misses += 1
        return None

    def put(self, key, value):
        self._storage[key] = value

    @property
    def hits(self):
        return self._hits

    @property
    def misses(self):
        return self._misses

    @property
    def hit_rate(self):
        total = self._hits + self._misses
        return 0.0 if total == 0 else self._hits / total
```

## Tests & What They Prove

### Cache Hit Tracking
```java
cache.put("user:123", userData);
V result = cache.get("user:123");
// Test: cache.hits == 1, cache.misses == 0
```
**Proves:** Cache correctly tracks when data is found.

### Cache Miss Tracking
```java
V result = cache.get("missing");
// Test: cache.misses == 1, result == null
```
**Proves:** Cache correctly tracks when data is not found.

### Hit Rate Calculation
```java
// After 3 hits and 1 miss
// Test: cache.getHitRate() == 0.75
```
**Proves:** Cache provides accurate performance metrics.

### Cache-Aside Pattern
```java
public User getUser(String userId) {
    User user = cache.get(userId);
    if (user == null) {
        user = database.getUser(userId);  // Expensive operation
        cache.put(userId, user);
    }
    return user;
}
```
**Proves:** Cache integrates properly with data sources.

## Common Misconceptions

❌ **"Cache hits are always better than misses"**
- Reality: First access is always a miss - this is normal and expected

❌ **"High hit rate = good cache"**
- Reality: Hit rate depends on access patterns; 80% might be excellent or terrible depending on the workload

❌ **"Cache should never miss"**
- Reality: Caches miss for new data, evicted data, and during startup

❌ **"Cache statistics don't matter"**
- Reality: Hit/miss ratios are crucial for tuning and monitoring cache effectiveness

## Navigation

⬅️ [Back to Root](../README.md)
