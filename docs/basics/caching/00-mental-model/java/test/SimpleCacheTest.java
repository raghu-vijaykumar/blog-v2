import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SimpleCache - validating mental model of caching.
 *
 * These tests prove the fundamental cache invariants:
 * - Cache hit: Returns stored data instantly
 * - Cache miss: Returns null for non-existent keys
 * - Overwrite: Correctly updates values
 */
public class SimpleCacheTest {

    private SimpleCache<String, String> cache;

    @BeforeEach
    void setUp() {
        cache = new SimpleCache<>();
    }

    @Test
    void cacheHit_ReturnsStoredValue() {
        // Given: Data is cached
        cache.put("user:123", "John Doe");

        // When: Same key is requested
        String result = cache.get("user:123");

        // Then: Returns cached value instantly (cache hit)
        assertEquals("John Doe", result, "Cache hit should return stored value");
        assertTrue(cache.containsKey("user:123"), "Key should exist after caching");
    }

    @Test
    void cacheMiss_ReturnsNull() {
        // When: Non-existent key is requested
        String result = cache.get("nonexistent");

        // Then: Returns null (cache miss)
        assertNull(result, "Cache miss should return null");
        assertFalse(cache.containsKey("nonexistent"), "Non-existent key should not be found");
    }

    @Test
    void put_StoresValue() {
        // When: Value is stored
        cache.put("config:timeout", "30s");

        // Then: Can be retrieved
        assertEquals("30s", cache.get("config:timeout"));
        assertEquals(1, cache.size(), "Cache should contain one item");
    }

    @Test
    void overwrite_UpdatesExistingValue() {
        // Given: Initial value
        cache.put("setting", "old_value");

        // When: Same key is updated
        cache.put("setting", "new_value");

        // Then: Only new value exists
        assertEquals("new_value", cache.get("setting"));
        assertEquals(1, cache.size(), "Should still be one item after overwrite");
    }

    @Test
    void multipleKeys_StoredIndependently() {
        // Given: Multiple different keys
        cache.put("user:1", "Alice");
        cache.put("user:2", "Bob");
        cache.put("product:100", "Laptop");

        // Then: All can be retrieved independently
        assertEquals("Alice", cache.get("user:1"));
        assertEquals("Bob", cache.get("user:2"));
        assertEquals("Laptop", cache.get("product:100"));
        assertEquals(3, cache.size());
    }

    @Test
    void emptyCache_SizeIsZero() {
        assertEquals(0, cache.size(), "Empty cache should have size 0");
        assertFalse(cache.containsKey("any"), "Empty cache contains no keys");
    }
}
