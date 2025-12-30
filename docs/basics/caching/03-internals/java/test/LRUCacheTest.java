import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LRU Cache - validating eviction policies and cache internals.
 *
 * These tests prove LRU behavior and demonstrate the trade-offs in cache design.
 */
public class LRUCacheTest {

    @Test
    void constructor_RejectsInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new LRUCache<Integer, String>(0));
        assertThrows(IllegalArgumentException.class, () -> new LRUCache<Integer, String>(-1));
    }

    @Test
    void capacity_EnforcedCorrectly() {
        LRUCache<Integer, String> cache = new LRUCache<>(2);

        cache.put(1, "one");
        cache.put(2, "two");
        assertEquals(2, cache.size());

        cache.put(3, "three"); // Should evict oldest (1)
        assertEquals(2, cache.size());
        assertFalse(cache.containsKey(1));
        assertTrue(cache.containsKey(2));
        assertTrue(cache.containsKey(3));
    }

    @Test
    void access_UpdatesRecency() {
        LRUCache<String, String> cache = new LRUCache<>(3);

        cache.put("a", "1");
        cache.put("b", "2");
        cache.put("c", "3");

        // Access "a" - should become most recent
        cache.get("a");

        // Add "d" - should evict "b" (least recently used), not "a"
        cache.put("d", "4");

        assertTrue(cache.containsKey("a"));  // Recently accessed
        assertTrue(cache.containsKey("c"));
        assertTrue(cache.containsKey("d"));
        assertFalse(cache.containsKey("b")); // Evicted LRU
    }

    @Test
    void put_UpdatesExistingEntryRecency() {
        LRUCache<String, String> cache = new LRUCache<>(2);

        cache.put("a", "1");
        cache.put("b", "2");

        // Update "a" - should become most recent
        cache.put("a", "updated");

        // Add "c" - should evict "b", not "a"
        cache.put("c", "3");

        assertEquals("updated", cache.get("a"));
        assertFalse(cache.containsKey("b"));
        assertTrue(cache.containsKey("c"));
    }

    @Test
    void get_ReturnsNullForMissingKey() {
        LRUCache<String, String> cache = new LRUCache<>(2);

        assertNull(cache.get("missing"));
        assertNull(cache.get("nonexistent"));
    }

    @Test
    void eviction_OrderIsCorrect() {
        LRUCache<String, String> cache = new LRUCache<>(3);

        cache.put("first", "1");
        cache.put("second", "2");
        cache.put("third", "3");

        // Access pattern: first, third, first, second
        cache.get("first");   // first becomes most recent
        cache.get("third");   // third becomes most recent
        cache.get("first");   // first becomes most recent
        cache.get("second");  // second becomes most recent

        // Add fourth - should evict "third" (now LRU)
        cache.put("fourth", "4");

        assertTrue(cache.containsKey("first"));
        assertTrue(cache.containsKey("second"));
        assertTrue(cache.containsKey("fourth"));
        assertFalse(cache.containsKey("third"));
    }

    @Test
    void capacity_One_ItemCache() {
        LRUCache<String, String> cache = new LRUCache<>(1);

        cache.put("a", "1");
        assertEquals("1", cache.get("a"));

        cache.put("b", "2"); // Should evict "a"
        assertNull(cache.get("a"));
        assertEquals("2", cache.get("b"));
    }

    @Test
    void clear_RemovesAllEntries() {
        LRUCache<String, String> cache = new LRUCache<>(3);

        cache.put("a", "1");
        cache.put("b", "2");
        cache.put("c", "3");
        assertEquals(3, cache.size());

        cache.clear();
        assertEquals(0, cache.size());
        assertTrue(cache.isEmpty());
        assertNull(cache.get("a"));
    }

    @Test
    void utilization_CalculatedCorrectly() {
        LRUCache<String, String> cache = new LRUCache<>(4);

        assertEquals(0.0, cache.getUtilization(), 0.001);

        cache.put("a", "1");
        assertEquals(0.25, cache.getUtilization(), 0.001);

        cache.put("b", "2");
        cache.put("c", "3");
        assertEquals(0.75, cache.getUtilization(), 0.001);

        cache.put("d", "4");
        assertEquals(1.0, cache.getUtilization(), 0.001);

        cache.put("e", "5"); // Evicts one
        assertEquals(1.0, cache.getUtilization(), 0.001);
    }

    @Test
    void keysInLRUOrder_ReturnsCorrectOrder() {
        LRUCache<String, String> cache = new LRUCache<>(3);

        cache.put("a", "1");
        cache.put("b", "2");
        cache.put("c", "3");

        // Access "a" to make it most recent
        cache.get("a");

        // Keys should be in LRU order: b, c, a (b oldest, a newest)
        var keys = cache.keysInLRUOrder();
        var iterator = keys.iterator();

        assertEquals("b", iterator.next()); // LRU
        assertEquals("c", iterator.next());
        assertEquals("a", iterator.next()); // MRU
    }

    @Test
    void largeCapacity_NoEviction() {
        LRUCache<Integer, String> cache = new LRUCache<>(100);

        for (int i = 0; i < 50; i++) {
            cache.put(i, "value" + i);
        }

        assertEquals(50, cache.size());

        // All should still be there
        for (int i = 0; i < 50; i++) {
            assertEquals("value" + i, cache.get(i));
        }
    }
}
