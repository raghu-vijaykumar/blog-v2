"""
Tests for SimpleCache - validating mental model of caching.

These tests prove the fundamental cache invariants:
- Cache hit: Returns stored data instantly
- Cache miss: Returns None for non-existent keys
- Overwrite: Correctly updates values
"""

import pytest
from src.simple_cache import SimpleCache


class TestSimpleCache:
    """Test suite for SimpleCache demonstrating cache mental model."""

    def setup_method(self):
        """Create fresh cache for each test."""
        self.cache = SimpleCache()

    def test_cache_hit_returns_stored_value(self):
        """Cache hit: Returns stored value instantly."""
        # Given: Data is cached
        self.cache.put("user:123", "John Doe")

        # When: Same key is requested
        result = self.cache.get("user:123")

        # Then: Returns cached value instantly (cache hit)
        assert result == "John Doe"
        assert self.cache.contains_key("user:123")

    def test_cache_miss_returns_none(self):
        """Cache miss: Returns None for non-existent keys."""
        # When: Non-existent key is requested
        result = self.cache.get("nonexistent")

        # Then: Returns None (cache miss)
        assert result is None
        assert not self.cache.contains_key("nonexistent")

    def test_put_stores_value(self):
        """Put operation stores value for retrieval."""
        # When: Value is stored
        self.cache.put("config:timeout", "30s")

        # Then: Can be retrieved
        assert self.cache.get("config:timeout") == "30s"
        assert self.cache.size() == 1

    def test_overwrite_updates_existing_value(self):
        """Overwrite: Updates existing value correctly."""
        # Given: Initial value
        self.cache.put("setting", "old_value")

        # When: Same key is updated
        self.cache.put("setting", "new_value")

        # Then: Only new value exists
        assert self.cache.get("setting") == "new_value"
        assert self.cache.size() == 1

    def test_multiple_keys_stored_independently(self):
        """Multiple keys are stored and retrieved independently."""
        # Given: Multiple different keys
        self.cache.put("user:1", "Alice")
        self.cache.put("user:2", "Bob")
        self.cache.put("product:100", "Laptop")

        # Then: All can be retrieved independently
        assert self.cache.get("user:1") == "Alice"
        assert self.cache.get("user:2") == "Bob"
        assert self.cache.get("product:100") == "Laptop"
        assert self.cache.size() == 3

    def test_empty_cache_size_is_zero(self):
        """Empty cache has size 0 and contains no keys."""
        assert self.cache.size() == 0
        assert not self.cache.contains_key("any")

    def test_clear_removes_all_data(self):
        """Clear operation removes all cached data."""
        # Given: Cache with data
        self.cache.put("key1", "value1")
        self.cache.put("key2", "value2")
        assert self.cache.size() == 2

        # When: Cleared
        self.cache.clear()

        # Then: Empty
        assert self.cache.size() == 0
        assert self.cache.get("key1") is None
        assert self.cache.get("key2") is None
