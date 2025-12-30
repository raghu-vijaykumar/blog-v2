# 02 – Real-world Usage Patterns

## Concept

This concept demonstrates how caching integrates into real systems, showing practical patterns like database integration, API caching, cache warming, and handling concurrent access.

## Why This Exists

While basics show *how* caching works, real-world usage shows *where and when* to apply caching in complex systems with databases, APIs, and multiple services.

**What problem does this solve?**
- Integrates caching with existing system components
- Handles practical scenarios like startup, concurrent access, and data consistency
- Demonstrates caching in multi-tier architectures

**Why does this exist?**
Most systems aren't greenfield - they have databases, APIs, and complex interactions that require careful cache integration.

## Mental Model

```
Real-World System:
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │────│ API Gateway │────│   Cache     │
└─────────────┘    └─────────────┘    └─────────────┘
                          │                 │
                          ▼                 ▼
                   ┌─────────────┐    ┌─────────────┐
                   │  Database   │    │ External API│
                   └─────────────┘    └─────────────┘
                          ▲                 ▲
                          └─────────────────┘
                       Cache warming on startup
```

## Code Walkthrough

Implements a complete user service with database simulation, demonstrating real-world caching patterns.

### Java Implementation
[View Java code](java/src/UserService.java)

```java
public class UserService {
    private final BasicCache<String, User> cache;
    private final Database database; // Simulated

    public UserService(BasicCache<String, User> cache, Database database) {
        this.cache = cache;
        this.database = database;
    }

    public User getUser(String userId) {
        // Cache-aside pattern
        User user = cache.get(userId);
        if (user != null) {
            return user; // Cache hit
        }

        // Cache miss - fetch from database
        user = database.getUser(userId);
        if (user != null) {
            cache.put(userId, user);
        }
        return user;
    }

    public void updateUser(String userId, User updatedUser) {
        // Write-through: Update database first
        database.updateUser(userId, updatedUser);

        // Then update cache
        cache.put(userId, updatedUser);
    }
}
```

## Tests & What They Prove

### Cache-Aside Integration
```java
UserService service = new UserService(cache, database);
User user = service.getUser("user:123");
// Test: Database called once, subsequent calls use cache
```
**Proves:** Cache properly integrates with data sources.

### Write-Through Consistency
```java
service.updateUser("user:123", updatedUser);
// Test: Database and cache both updated
```
**Proves:** Updates maintain consistency between cache and database.

### Cache Warming
```java
service.warmCache(Arrays.asList("user:1", "user:2"));
// Test: Cache populated without client requests
```
**Proves:** Startup performance optimization through pre-population.

### Concurrent Access
```java
// Multiple threads accessing same user
// Test: No race conditions, consistent behavior
```
**Proves:** Cache handles real-world concurrent scenarios.

## Common Misconceptions

❌ **"Cache replaces database"**
- Reality: Cache augments database, doesn't replace it

❌ **"Cache everything on startup"**
- Reality: Cache warming should be selective for frequently accessed data

❌ **"Updates only need to go to cache"**
- Reality: Write-through ensures consistency with persistent storage

❌ **"Caching is transparent"**
- Reality: Application code must explicitly manage cache operations

❌ **"One cache fits all scenarios"**
- Reality: Different data types need different caching strategies

## Navigation

⬅️ [Previous: 01 – Basics](../01-basics/README.md)
➡️ [Next: 03 – Internals](../03-internals/README.md)
