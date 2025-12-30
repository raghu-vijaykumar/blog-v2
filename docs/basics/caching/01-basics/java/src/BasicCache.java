import java.util.HashMap;
import java.util.Map;

/**
 * Basic Cache with Hit/Miss Tracking.
 *
 * Demonstrates fundamental cache operations and statistics collection.
 * This builds on the mental model by adding observability.
 */
public class BasicCache<K, V> {
    private final Map<K, V> storage = new HashMap<>();
    private int hits = 0;
    private int misses = 0;

    /**
     * Retrieves a value from the cache, tracking hits and misses.
     *
     * @param key the key to look up
     * @return the cached value, or null if not present (cache miss)
     */
    public V get(K key) {
        V value = storage.get(key);
        if (value != null) {
            hits++;
            return value;
        }
        misses++;
        return null;
    }

    /**
     * Stores a value in the cache.
     *
     * @param key the key to store under
     * @param value the value to cache
     */
    public void put(K key, V value) {
        storage.put(key, value);
    }

    /**
     * Returns the number of cache hits since creation.
     */
    public int getHits() {
        return hits;
    }

    /**
     * Returns the number of cache misses since creation.
     */
    public int getMisses() {
        return misses;
    }

    /**
     * Calculates the cache hit rate.
     *
     * @return hit rate as a fraction (0.0 to 1.0), or 0.0 if no requests
     */
    public double getHitRate() {
        int total = hits + misses;
        return total == 0 ? 0.0 : (double) hits / total;
    }

    /**
     * Returns the current number of items in the cache.
     */
    public int size() {
        return storage.size();
    }

    /**
     * Clears all cached data and resets statistics.
     */
    public void clear() {
        storage.clear();
        hits = 0;
        misses = 0;
    }
}
