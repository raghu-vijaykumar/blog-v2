# 04 – Advanced Patterns

## Concept

This concept covers sophisticated caching strategies including distributed caching, cache invalidation patterns, write-through vs write-behind, and handling cache consistency in distributed systems.

## Why This Exists

Simple caching works for single applications, but distributed systems introduce complex challenges around consistency, coordination, and failure handling.

**What problem does this solve?**
- Enables caching in distributed environments
- Manages cache consistency across multiple nodes
- Handles cache invalidation in complex architectures

**Why does this exist?**
Modern systems are distributed - without advanced patterns, caching becomes a source of bugs rather than performance gains.

## Mental Model

```
Distributed Cache Cluster:
┌─────────────┐    ┌─────────────────┐    ┌─────────────┐
│   Node A    │────│  Cache Cluster  │────│   Node B    │
│             │    │                 │    │             │
│ Cache:      │    │ ┌─────────────┐ │    │ Cache:      │
│ ├─ user:1   │    │ │   user:1    │ │    │ ├─ user:1   │
│ └─ user:2   │    │ │   user:2    │ │    │ └─ user:2   │
└─────────────┘    │ └─────────────┘ │    └─────────────┘
                   └─────────────────┘
                          │
                   ┌─────────────┐
                   │  Database  │
                   │   (Source  │
                   │     of     │
                   │    Truth)  │
                   └─────────────┘

Consistency Challenges:
- Cache invalidation across nodes
- Race conditions on updates
- Network partitions
- Eventual consistency vs strong consistency
```

## Code Walkthrough

Implements distributed cache simulation with consistency protocols, invalidation strategies, and failure handling.

### Java Implementation
[View Java code](java/src/DistributedCache.java)

```java
public class DistributedCache {
    private final Map<String, CacheNode> nodes = new HashMap<>();
    private final Database database;

    public void invalidate(String key) {
        // Broadcast invalidation to all nodes
        for (CacheNode node : nodes.values()) {
            node.invalidate(key);
        }
    }

    public void writeThrough(String key, String value) {
        // Update database first
        database.update(key, value);

        // Then broadcast to all cache nodes
        for (CacheNode node : nodes.values()) {
            node.put(key, value);
        }
    }
}
```

## Tests & What They Prove

### Invalidation Consistency
```java
// Test: Update on one node invalidates others
nodeA.put("user:123", "old");
nodeB.get("user:123"); // Should hit cache

cluster.invalidate("user:123");
// Test: All nodes return null for invalidated key
```
**Proves:** Distributed invalidation maintains consistency across nodes.

### Write-Through Consistency
```java
cluster.writeThrough("config:timeout", "60s");
// Test: Database and all caches updated synchronously
```
**Proves:** Write-through ensures strong consistency at cost of latency.

### Write-Behind Performance
```java
cluster.writeBehind("metrics:counter", "100");
// Test: Caches updated immediately, database eventually
```
**Proves:** Write-behind improves performance with eventual consistency.

### Node Failure Handling
```java
nodeB.fail(); // Simulate node failure
cluster.put("key", "value");
// Test: Other nodes still work, failed node misses updates
```
**Proves:** System remains functional despite individual node failures.

## Common Misconceptions

❌ **"Distributed cache = shared memory"**
- Reality: Network latency and failures make coordination complex

❌ **"Eventual consistency is always acceptable"**
- Reality: Some operations require strong consistency (financial transactions)

❌ **"Cache invalidation is simple"**
- Reality: Invalidation strategies (write-through, write-behind, TTL) have different trade-offs

❌ **"All nodes see updates instantly"**
- Reality: Network delays and partitions cause temporary inconsistencies

❌ **"Distributed caching eliminates database load"**
- Reality: Cache misses, invalidation storms, and coordination still hit the database

## Navigation

⬅️ [Previous: 03 – Internals](../03-internals/README.md)
➡️ [Next: 05 – Failures](../05-failures/README.md)
