"""
Tests for BasicCache - validating cache operations and statistics.

These tests prove the fundamental cache behaviors and monitoring capabilities.
"""

import pytest
from src.basic_cache import BasicCache, CacheAsidePattern


class TestBasicCache:
    """Test suite for BasicCache with statistics tracking."""

    def setup_method(self):
        """Create fresh cache for each test."""
        self.cache = BasicCache()

    def test_get_tracks_hits_correctly(self):
        """Cache hit: Returns stored value and increments hit counter."""
        # Given: Data is cached
        self.cache.put("user:123", "John Doe")

        # When: Same key is accessed
        result = self.cache.get("user:123")

        # Then: Returns correct value and tracks hit
        assert result == "John Doe"
        assert self.cache.hits == 1
        assert self.cache.misses == 0
        assert self.cache.hit_rate == 1.0

    def test_get_tracks_misses_correctly(self):
        """Cache miss: Returns None and increments miss counter."""
        # When: Non-existent key is accessed
        result = self.cache.get("nonexistent")

        # Then: Returns None and tracks miss
        assert result is None
        assert self.cache.hits == 0
        assert self.cache.misses == 1
        assert self.cache.hit_rate == 0.0

    def test_get_calculates_hit_rate_correctly(self):
        """Hit rate calculation handles mix of hits and misses."""
        # Given: Cache with some data
        self.cache.put("key1", "value1")
        self.cache.put("key2", "value2")

        # 3 hits, 2 misses
        self.cache.get("key1")     # hit
        self.cache.get("key2")     # hit
        self.cache.get("missing1") # miss
        self.cache.get("key1")     # hit
        self.cache.get("missing2") # miss

        # Then: Correct statistics
        assert self.cache.hits == 3
        assert self.cache.misses == 2
        assert self.cache.hit_rate == 0.6  # 3/5 = 0.6

    def test_put_stores_data(self):
        """Put operation stores data for retrieval."""
        # When: Data is stored
        self.cache.put("config:timeout", "30s")

        # Then: Can be retrieved and size increases
        assert self.cache.get("config:timeout") == "30s"
        assert self.cache.size == 1
        assert self.cache.hits == 1

    def test_size_returns_correct_count(self):
        """Size reflects current number of cached items."""
        assert self.cache.size == 0

        self.cache.put("a", "1")
        assert self.cache.size == 1

        self.cache.put("b", "2")
        self.cache.put("c", "3")
        assert self.cache.size == 3

    def test_clear_resets_everything(self):
        """Clear removes all data and resets statistics."""
        # Given: Cache with data and statistics
        self.cache.put("key", "value")
        self.cache.get("key")      # creates 1 hit
        self.cache.get("missing")  # creates 1 miss

        assert self.cache.size == 1
        assert self.cache.hits == 1
        assert self.cache.misses == 1

        # When: Cleared
        self.cache.clear()

        # Then: Everything reset
        assert self.cache.size == 0
        assert self.cache.hits == 0
        assert self.cache.misses == 0
        assert self.cache.hit_rate == 0.0
        assert self.cache.get("key") is None

    def test_hit_rate_handles_empty_cache(self):
        """Empty cache returns 0.0 hit rate."""
        assert self.cache.hit_rate == 0.0

    def test_statistics_persist_across_operations(self):
        """Statistics accumulate correctly across operations."""
        # Statistics should persist across operations
        self.cache.put("a", "1")
        self.cache.get("a")   # hit
        self.cache.get("b")   # miss
        self.cache.get("a")   # hit

        assert self.cache.hits == 2
        assert self.cache.misses == 1
        assert self.cache.hit_rate == pytest.approx(2.0/3.0)

        # Adding more data doesn't reset stats
        self.cache.put("c", "3")
        self.cache.get("c")   # hit

        assert self.cache.hits == 3
        assert self.cache.misses == 1
        assert self.cache.hit_rate == pytest.approx(3.0/4.0)


class TestCacheAsidePattern:
    """Test the cache-aside pattern implementation."""

    def setup_method(self):
        """Create cache and simulated database."""
        self.cache = BasicCache()
        # Simulate database as a dict
        self.database = {
            "user:1": {"id": 1, "name": "Alice", "email": "alice@example.com"},
            "user:2": {"id": 2, "name": "Bob", "email": "bob@example.com"}
        }
        self.service = CacheAsidePattern(self.cache, self.database)

    def test_cache_aside_first_request_misses(self):
        """First request for user goes to database and populates cache."""
        # When: First request for user
        user = self.service.get_user("user:1")

        # Then: Returns correct data, cache miss occurred
        assert user == {"id": 1, "name": "Alice", "email": "alice@example.com"}
        assert self.cache.hits == 0
        assert self.cache.misses == 1
        assert self.cache.size == 1

    def test_cache_aside_subsequent_requests_hit(self):
        """Subsequent requests for same user hit the cache."""
        # First request (miss)
        self.service.get_user("user:1")
        assert self.cache.hits == 0
        assert self.cache.misses == 1

        # Second request (hit)
        user = self.service.get_user("user:1")

        # Then: Returns from cache
        assert user == {"id": 1, "name": "Alice", "email": "alice@example.com"}
        assert self.cache.hits == 1
        assert self.cache.misses == 1  # Still 1 miss
        assert self.cache.size == 1

    def test_cache_aside_nonexistent_user(self):
        """Requests for non-existent users return None without caching."""
        # When: Request for non-existent user
        user = self.service.get_user("user:999")

        # Then: Returns None, no caching
        assert user is None
        assert self.cache.hits == 0
        assert self.cache.misses == 1
        assert self.cache.size == 0  # Nothing cached

    def test_cache_aside_multiple_users(self):
        """Multiple different users are cached independently."""
        # Request different users
        user1 = self.service.get_user("user:1")
        user2 = self.service.get_user("user:2")

        assert user1["name"] == "Alice"
        assert user2["name"] == "Bob"

        # Both cached
        assert self.cache.size == 2

        # Subsequent requests hit cache
        self.service.get_user("user:1")  # hit
        self.service.get_user("user:2")  # hit

        assert self.cache.hits == 2
        assert self.cache.misses == 2  # Initial requests
