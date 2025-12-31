import java.util.HashMap;
import java.util.Map;

/**
 * Basic cache interface for real-world usage.
 * Simplified version for this concept.
 */
public interface BasicCache<K, V> {
    V get(K key);
    void put(K key, V value);
    int getHits();
    int getMisses();
    double getHitRate();
    int size();
}

/**
 * Simple cache implementation with hit/miss tracking.
 */
class SimpleCache<K, V> implements BasicCache<K, V> {
    private final Map<K, V> storage = new HashMap<>();
    private int hits = 0;
    private int misses = 0;

    @Override
    public V get(K key) {
        V value = storage.get(key);
        if (value != null) {
            hits++;
            return value;
        }
        misses++;
        return null;
    }

    @Override
    public void put(K key, V value) {
        storage.put(key, value);
    }

    @Override
    public int getHits() { return hits; }

    @Override
    public int getMisses() { return misses; }

    @Override
    public double getHitRate() {
        int total = hits + misses;
        return total == 0 ? 0.0 : (double) hits / total;
    }

    @Override
    public int size() { return storage.size(); }
}
