import java.util.List;

/**
 * User service demonstrating real-world caching patterns.
 *
 * Shows cache-aside, write-through, and cache warming patterns.
 */
public class UserService {
    private final BasicCache<String, User> cache;
    private final Database database;

    public UserService(BasicCache<String, User> cache, Database database) {
        this.cache = cache;
        this.database = database;
    }

    /**
     * Get user using cache-aside pattern.
     * 1. Try cache first
     * 2. If miss, fetch from database
     * 3. Store in cache for future requests
     */
    public User getUser(String userId) {
        // Check cache first
        User user = cache.get(userId);
        if (user != null) {
            return user; // Cache hit
        }

        // Cache miss - fetch from database
        user = database.getUser(userId);
        if (user != null) {
            // Store in cache for future requests
            cache.put(userId, user);
        }

        return user;
    }

    /**
     * Update user using write-through pattern.
     * 1. Update database first
     * 2. Then update cache
     */
    public void updateUser(String userId, User updatedUser) {
        // Write-through: Update database first
        database.updateUser(userId, updatedUser);

        // Then update cache to maintain consistency
        cache.put(userId, updatedUser);
    }

    /**
     * Create new user.
     * For new data, we don't cache immediately - let it be cached on first read.
     */
    public void createUser(User user) {
        database.createUser(user);
        // Don't cache immediately - will be cached on first read
    }

    /**
     * Warm cache with frequently accessed users.
     * Useful for startup performance optimization.
     */
    public void warmCache(List<String> userIds) {
        for (String userId : userIds) {
            User user = database.getUser(userId);
            if (user != null) {
                cache.put(userId, user);
            }
        }
    }

    /**
     * Get cache statistics for monitoring.
     */
    public CacheStats getCacheStats() {
        return new CacheStats(
            cache.getHits(),
            cache.getMisses(),
            cache.getHitRate(),
            cache.size()
        );
    }

    /**
     * Cache statistics for monitoring and alerting.
     */
    public static class CacheStats {
        public final int hits;
        public final int misses;
        public final double hitRate;
        public final int size;

        public CacheStats(int hits, int misses, double hitRate, int size) {
            this.hits = hits;
            this.misses = misses;
            this.hitRate = hitRate;
            this.size = size;
        }
    }
}
