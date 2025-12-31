---
sidebar_label: "Internals & Trade-offs"
---

# Internals & Trade-offs

## Concept

This concept dives into how caches work under the hood, exploring eviction policies (LRU, LFU, FIFO), memory management, and the fundamental trade-offs in cache design.

## Why This Exists

Understanding internals is crucial because cache behavior isn't magic - it's carefully engineered trade-offs that affect system performance and reliability.

**What problem does this solve?**
- Explains why caches behave the way they do
- Helps choose appropriate eviction policies
- Enables debugging cache-related performance issues

**Why does this exist?**
Without understanding internals, developers make incorrect assumptions about cache behavior, leading to bugs and performance issues.

## Mental Model

```
Cache Memory (Limited Size):
[Most Recently Used] ←→ [Least Recently Used]
     Hot Data              Cold Data ← Evicted

Access Patterns:
- Read hot data: O(1) hit
- Read cold data: Cache miss, eviction of LRU
- Write: Update recency, potentially evict LRU

Trade-offs:
Performance vs Memory vs Consistency
     ↓         ↓         ↓
  Speed    Efficiency  Correctness
```

## Code Walkthrough

Implements LRU cache with configurable size limits, demonstrating eviction behavior and the cost of maintaining access order.

### Java Implementation
[View Java code](java/src/LRUCache.java) | [View tests](java/test/LRUCacheTest.java)

```java
public class LRUCache<K, V> {
    private final int capacity;
    private final Map<K, V> cache = new LinkedHashMap<>(16, 0.75f, true);

    public LRUCache(int capacity) {
        this.capacity = capacity;
    }

    public V get(K key) {
        return cache.get(key); // LinkedHashMap maintains access order
    }

    public void put(K key, V value) {
        cache.put(key, value);
        if (cache.size() > capacity) {
            // Remove eldest entry (LRU)
            cache.remove(cache.keySet().iterator().next());
        }
    }
}
```

### Python Implementation
[View Python code](python/src/lru_cache.py) | [View tests](python/tests/test_lru_cache.py)

```python
class LRUCache:
    def __init__(self, capacity):
        self.capacity = capacity
        self.cache = OrderedDict()

    def get(self, key):
        if key in self.cache:
            # Move to end (most recently used)
            self.cache.move_to_end(key)
            return self.cache[key]
        return None

    def put(self, key, value):
        if key in self.cache:
            # Update existing, move to end
            self.cache.move_to_end(key)
        self.cache[key] = value

        if len(self.cache) > self.capacity:
            # Remove least recently used (first item)
            self.cache.popitem(last=False)
```

## Tests & What They Prove

### LRU Eviction
```java
LRUCache cache = new LRUCache(2);
cache.put("a", "1"); cache.put("b", "2");
cache.get("a");      // "a" now most recently used
cache.put("c", "3"); // Evicts "b" (LRU)
assert cache.get("b") == null;
```
**Proves:** LRU correctly evicts least recently used items when capacity exceeded.

### Access Order Updates
```java
cache.put("a", "1"); cache.put("b", "2"); cache.put("c", "3");
cache.get("a");      // "a" becomes most recently used
cache.put("d", "4"); // Evicts "b", not "a"
assert cache.get("a") != null;
assert cache.get("b") == null;
```
**Proves:** Access operations update recency ordering.

### Capacity Enforcement
```java
LRUCache cache = new LRUCache(1);
cache.put("a", "1");
cache.put("b", "2"); // Should evict "a"
assert cache.get("a") == null;
assert cache.get("b") == "2";
```
**Proves:** Cache never exceeds configured capacity.

## Common Misconceptions

❌ **"LRU is always best"**
- Reality: LRU works well for temporal locality but poorly for one-time scans

❌ **"Eviction is instant and free"**
- Reality: Maintaining access order has O(1) amortized but O(n) worst-case cost

❌ **"Cache size = memory usage"**
- Reality: Metadata, pointers, and fragmentation add overhead

❌ **"All caches use the same eviction policy"**
- Reality: Different workloads need different policies (LFU for frequency, ARC for mixed patterns)

❌ **"Bigger cache is always better"**
- Reality: Large caches increase memory pressure and GC pauses

## Navigation

⬅️ [Previous: 02 – Real-world](../02-real-world/README.md)
➡️ [Next: 04 – Advanced](../04-advanced/README.md)
