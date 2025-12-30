import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BasicCache - validating cache operations and statistics.
 *
 * These tests prove the fundamental cache behaviors and monitoring capabilities.
 */
public class BasicCacheTest {

    private BasicCache<String, String> cache;

    @BeforeEach
    void setUp() {
        cache = new BasicCache<>();
    }

    @Test
    void get_TracksHitsCorrectly() {
        // Given: Data is cached
        cache.put("user:123", "John Doe");

        // When: Same key is accessed
        String result = cache.get("user:123");

        // Then: Returns correct value and tracks hit
        assertEquals("John Doe", result);
        assertEquals(1, cache.getHits());
        assertEquals(0, cache.getMisses());
        assertEquals(1.0, cache.getHitRate(), 0.001);
    }

    @Test
    void get_TracksMissesCorrectly() {
        // When: Non-existent key is accessed
        String result = cache.get("nonexistent");

        // Then: Returns null and tracks miss
        assertNull(result);
        assertEquals(0, cache.getHits());
        assertEquals(1, cache.getMisses());
        assertEquals(0.0, cache.getHitRate(), 0.001);
    }

    @Test
    void get_CalculatesHitRateCorrectly() {
        // Given: Mix of hits and misses
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        // 3 hits, 2 misses
        cache.get("key1");     // hit
        cache.get("key2");     // hit
        cache.get("missing1"); // miss
        cache.get("key1");     // hit
        cache.get("missing2"); // miss

        // Then: Correct statistics
        assertEquals(3, cache.getHits());
        assertEquals(2, cache.getMisses());
        assertEquals(0.6, cache.getHitRate(), 0.001); // 3/5 = 0.6
    }

    @Test
    void put_StoresData() {
        // When: Data is stored
        cache.put("config:timeout", "30s");

        // Then: Can be retrieved and size increases
        assertEquals("30s", cache.get("config:timeout"));
        assertEquals(1, cache.size());
        assertEquals(1, cache.getHits());
    }

    @Test
    void size_ReturnsCorrectCount() {
        assertEquals(0, cache.size());

        cache.put("a", "1");
        assertEquals(1, cache.size());

        cache.put("b", "2");
        cache.put("c", "3");
        assertEquals(3, cache.size());
    }

    @Test
    void clear_ResetsEverything() {
        // Given: Cache with data and statistics
        cache.put("key", "value");
        cache.get("key");      // creates 1 hit
        cache.get("missing");  // creates 1 miss

        assertEquals(1, cache.size());
        assertEquals(1, cache.getHits());
        assertEquals(1, cache.getMisses());

        // When: Cleared
        cache.clear();

        // Then: Everything reset
        assertEquals(0, cache.size());
        assertEquals(0, cache.getHits());
        assertEquals(0, cache.getMisses());
        assertEquals(0.0, cache.getHitRate(), 0.001);
        assertNull(cache.get("key"));
    }

    @Test
    void hitRate_HandlesEmptyCache() {
        // Empty cache should have 0.0 hit rate
        assertEquals(0.0, cache.getHitRate(), 0.001);
    }

    @Test
    void statistics_AreIndependentOfOperations() {
        // Statistics should persist across operations
        cache.put("a", "1");
        cache.get("a");   // hit
        cache.get("b");   // miss
        cache.get("a");   // hit

        assertEquals(2, cache.getHits());
        assertEquals(1, cache.getMisses());
        assertEquals(2.0/3.0, cache.getHitRate(), 0.001);

        // Adding more data doesn't reset stats
        cache.put("c", "3");
        cache.get("c");   // hit

        assertEquals(3, cache.getHits());
        assertEquals(1, cache.getMisses());
        assertEquals(3.0/4.0, cache.getHitRate(), 0.001);
    }
}
