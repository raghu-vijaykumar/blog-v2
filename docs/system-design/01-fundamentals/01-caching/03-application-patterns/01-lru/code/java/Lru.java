import java.util.*;

// LRU cache using LinkedHashMap in access-order.
// Run: javac Lru.java && java Lru
class Lru<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    public Lru(int capacity) {
        // true = accessOrder (not insertion order)
        super(Math.max(16, capacity * 2), 0.75f, true);
        if (capacity <= 0) throw new IllegalArgumentException("capacity > 0");
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }

    public V getValue(K key) {
        return super.getOrDefault(key, null);
    }

    public void putValue(K key, V value) {
        super.put(key, value);
    }

    // Minimal tests
    public static void main(String[] args) {
        Lru<String, Integer> lru = new Lru<>(2);
        lru.putValue("a", 1);
        lru.putValue("b", 2);
        assert Objects.equals(lru.getValue("a"), 1); // a becomes MRU, b is LRU
        lru.putValue("c", 3); // evicts b
        assert lru.getValue("b") == null;
        assert Objects.equals(lru.getValue("a"), 1);
        assert Objects.equals(lru.getValue("c"), 3);
        lru.putValue("d", 4); // evicts a
        assert lru.getValue("a") == null;
        assert Objects.equals(lru.getValue("c"), 3);
        assert Objects.equals(lru.getValue("d"), 4);
        System.out.println("LRU basic tests passed.");
    }
}
