import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU Cache implementation demonstrating eviction policies and cache internals.
 *
 * Uses LinkedHashMap with access-order to maintain LRU ordering efficiently.
 * Shows the trade-offs between memory usage, performance, and eviction accuracy.
 */
public class LRUCache<K, V> {
    private final int capacity;
    private final Map<K, V> cache;

    /**
     * Create LRU cache with specified capacity.
     *
     * @param capacity maximum number of entries
     */
    public LRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;

        // LinkedHashMap with access order (true) maintains LRU order
        // Initial capacity 16, load factor 0.75f for good performance
        this.cache = new LinkedHashMap<K, V>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                // Remove eldest when size exceeds capacity
                return size() > LRUCache.this.capacity;
            }
        };
    }

    /**
     * Get value from cache, updating access order.
     *
     * @param key the key to look up
     * @return cached value or null if not present
     */
    public V get(K key) {
        return cache.get(key); // LinkedHashMap handles access order update
    }

    /**
     * Put value in cache, evicting LRU entry if necessary.
     *
     * @param key the key
     * @param value the value
     */
    public void put(K key, V value) {
        cache.put(key, value);
        // LinkedHashMap's removeEldestEntry handles eviction automatically
    }

    /**
     * Check if key exists in cache.
     */
    public boolean containsKey(K key) {
        return cache.containsKey(key);
    }

    /**
     * Get current cache size.
     */
    public int size() {
        return cache.size();
    }

    /**
     * Get cache capacity.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Check if cache is empty.
     */
    public boolean isEmpty() {
        return cache.isEmpty();
    }

    /**
     * Clear all entries from cache.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Get current utilization as percentage.
     */
    public double getUtilization() {
        return (double) cache.size() / capacity;
    }

    /**
     * Get all keys in LRU order (eldest first).
     * Useful for debugging eviction behavior.
     */
    public Iterable<K> keysInLRUOrder() {
        return cache.keySet(); // LinkedHashMap maintains insertion/access order
    }
}
