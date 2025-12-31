import java.util.HashMap;
import java.util.Map;

/**
 * Mental Model: Simple in-memory cache demonstrating basic cache operations.
 *
 * This represents the "librarian's quick shelf" - a fast storage layer that
 * remembers frequently accessed data to avoid expensive backend operations.
 */
public class SimpleCache<K, V> {
    private final Map<K, V> storage = new HashMap<>();

    /**
     * Retrieves a value from the cache.
     *
     * @param key the key to look up
     * @return the cached value, or null if not present (cache miss)
     */
    public V get(K key) {
        return storage.get(key);
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
     * Returns the number of items currently cached.
     * Useful for understanding cache state.
     */
    public int size() {
        return storage.size();
    }

    /**
     * Checks if a key exists in the cache.
     */
    public boolean containsKey(K key) {
        return storage.containsKey(key);
    }
}
