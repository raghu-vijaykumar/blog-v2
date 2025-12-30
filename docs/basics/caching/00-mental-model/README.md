# 00 – Mental Model of Caching

## Concept

Caching is a performance optimization technique that stores frequently accessed data in a fast-access storage layer to avoid expensive operations. Think of it as a smart assistant who remembers answers to common questions so you don't have to look them up repeatedly.

## Why This Exists

In system design, every operation has a cost:
- **Database queries**: Network round-trips, disk I/O, query processing
- **API calls**: Network latency, service processing time
- **Computations**: CPU cycles, memory allocation

Caching solves the problem of **redundant expensive operations**. When data is requested multiple times, the cache provides instant access instead of recomputing or refetching.

**What problem does this solve?**
- Reduces response time from seconds to milliseconds
- Decreases load on backend systems
- Improves user experience and system scalability

**Why does this exist?**
Without caching, systems waste resources on repeated identical work. Real-world example: A news website recaching the same article data for thousands of readers.

## Mental Model

Imagine a busy library:

```
┌─────────────────────────────────────────────────┐
│                    PATRON                       │
│  "I need 'Design Patterns' book"                │
└─────────────────┬───────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────┐
│               LIBRARIAN (Cache)                 │
│                                                 │
│  Quick Shelf:                                   │
│  ┌─────────────────────────────────────────┐    │
│  │ "Design Patterns" ← Available instantly │    │
│  └─────────────────────────────────────────┘    │
│                                                 │
│  If not on quick shelf → Go to stacks (slow)    │
└─────────────────┬───────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────┐
│               BOOK STACKS (Backend)             │
│  Slow retrieval, limited copies, far away      │
└─────────────────────────────────────────────────┘
```

**Cache invariants:**
- **Cache hit**: Data exists in cache → instant return
- **Cache miss**: Data not in cache → fetch from backend, store in cache
- **Staleness**: Cached data can become outdated
- **Eviction**: Limited space means removing old data

## Code Walkthrough

This concept demonstrates the simplest possible cache: an in-memory key-value store.

### Java Implementation
[View Java code](java/src/SimpleCache.java) | [View tests](java/test/SimpleCacheTest.java)

```java
public class SimpleCache<K, V> {
    private final Map<K, V> storage = new HashMap<>();

    public V get(K key) {
        return storage.get(key);  // null if not present
    }

    public void put(K key, V value) {
        storage.put(key, value);
    }
}
```

### Python Implementation
[View Python code](python/src/simple_cache.py) | [View tests](python/tests/test_simple_cache.py)

```python
class SimpleCache:
    def __init__(self):
        self._storage = {}

    def get(self, key):
        return self._storage.get(key)  # None if not present

    def put(self, key, value):
        self._storage[key] = value
```

## Tests & What They Prove

Tests validate the fundamental cache behaviors:

### Cache Hit
```java
// Should return stored value immediately
cache.put("user:123", userData);
assert cache.get("user:123") == userData;
```
**Proves:** Cache preserves data and provides instant access.

### Cache Miss
```java
// Should return null/None for non-existent keys
assert cache.get("nonexistent") == null;
```
**Proves:** Cache doesn't invent data - only returns what was stored.

### Overwrite Behavior
```java
// Should replace old values with new ones
cache.put("key", "old");
cache.put("key", "new");
assert cache.get("key").equals("new");
```
**Proves:** Cache updates values correctly (no duplication).

## Common Misconceptions

❌ **"Caching just makes things faster"**
- Reality: Caching trades time for space and introduces consistency challenges

❌ **"Cache everything indefinitely"**
- Reality: Limited space requires eviction policies

❌ **"Cache = Database"**
- Reality: Cache is temporary storage, not persistent

❌ **"Caching is always beneficial"**
- Reality: Wrong caching can hurt performance (cache misses, stale data)

## Navigation

⬅️ [Back to Root](../README.md)
➡️ [Next: 01 – Basics](../01-basics/README.md)
