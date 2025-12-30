"""
Tests for LRU Cache - validating eviction policies and cache internals.

These tests prove LRU behavior and demonstrate the trade-offs in cache design.
"""

import pytest
from src.lru_cache import LRUCache


class TestLRUCache:
    """Test suite for LRU cache implementation."""

    def test_constructor_rejects_invalid_capacity(self):
        """Constructor should reject non-positive capacities."""
        with pytest.raises(ValueError, match="Capacity must be positive"):
            LRUCache(0)

        with pytest.raises(ValueError, match="Capacity must be positive"):
            LRUCache(-1)

    def test_capacity_enforced_correctly(self):
        """Cache should not exceed specified capacity."""
        cache = LRUCache(2)

        cache.put(1, "one")
        cache.put(2, "two")
        assert cache.size == 2

        cache.put(3, "three")  # Should evict oldest (1)
        assert cache.size == 2
        assert not cache.contains_key(1)
        assert cache.contains_key(2)
        assert cache.contains_key(3)

    def test_access_updates_recency(self):
        """Accessing an item should make it most recently used."""
        cache = LRUCache(3)

        cache.put("a", "1")
        cache.put("b", "2")
        cache.put("c", "3")

        # Access "a" - should become most recent
        cache.get("a")

        # Add "d" - should evict "b" (least recently used), not "a"
        cache.put("d", "4")

        assert cache.contains_key("a")   # Recently accessed
        assert cache.contains_key("c")
        assert cache.contains_key("d")
        assert not cache.contains_key("b")  # Evicted LRU

    def test_put_updates_existing_entry_recency(self):
        """Updating existing entry should move it to most recent."""
        cache = LRUCache(2)

        cache.put("a", "1")
        cache.put("b", "2")

        # Update "a" - should become most recent
        cache.put("a", "updated")

        # Add "c" - should evict "b", not "a"
        cache.put("c", "3")

        assert cache.get("a") == "updated"
        assert not cache.contains_key("b")
        assert cache.contains_key("c")

    def test_get_returns_none_for_missing_key(self):
        """Get should return None for keys not in cache."""
        cache = LRUCache(2)

        assert cache.get("missing") is None
        assert cache.get("nonexistent") is None

    def test_eviction_order_is_correct(self):
        """Complex access patterns should result in correct LRU eviction."""
        cache = LRUCache(3)

        cache.put("first", "1")
        cache.put("second", "2")
        cache.put("third", "3")

        # Access pattern: first, third, first, second
        cache.get("first")   # first becomes most recent
        cache.get("third")   # third becomes most recent
        cache.get("first")   # first becomes most recent
        cache.get("second")  # second becomes most recent

        # Add fourth - should evict "third" (now LRU)
        cache.put("fourth", "4")

        assert cache.contains_key("first")
        assert cache.contains_key("second")
        assert cache.contains_key("fourth")
        assert not cache.contains_key("third")

    def test_capacity_one_item_cache(self):
        """Single item cache should work correctly."""
        cache = LRUCache(1)

        cache.put("a", "1")
        assert cache.get("a") == "1"

        cache.put("b", "2")  # Should evict "a"
        assert cache.get("a") is None
        assert cache.get("b") == "2"

    def test_clear_removes_all_entries(self):
        """Clear should remove all entries."""
        cache = LRUCache(3)

        cache.put("a", "1")
        cache.put("b", "2")
        cache.put("c", "3")
        assert cache.size == 3

        cache.clear()
        assert cache.size == 0
        assert cache.is_empty()
        assert cache.get("a") is None

    def test_utilization_calculated_correctly(self):
        """Utilization should reflect current vs capacity ratio."""
        cache = LRUCache(4)

        assert cache.utilization == 0.0

        cache.put("a", "1")
        assert cache.utilization == 0.25

        cache.put("b", "2")
        cache.put("c", "3")
        assert cache.utilization == 0.75

        cache.put("d", "4")
        assert cache.utilization == 1.0

        cache.put("e", "5")  # Evicts one
        assert cache.utilization == 1.0

    def test_keys_in_lru_order_returns_correct_order(self):
        """Keys should be returned in LRU order (oldest first)."""
        cache = LRUCache(3)

        cache.put("a", "1")
        cache.put("b", "2")
        cache.put("c", "3")

        # Access "a" to make it most recent
        cache.get("a")

        # Keys should be in LRU order: b, c, a (b oldest, a newest)
        keys = cache.keys_in_lru_order()
        assert keys == ["b", "c", "a"]

    def test_large_capacity_no_eviction(self):
        """Large capacity should prevent unnecessary eviction."""
        cache = LRUCache(100)

        for i in range(50):
            cache.put(i, f"value{i}")

        assert cache.size == 50

        # All should still be there
        for i in range(50):
            assert cache.get(i) == f"value{i}"

    def test_repr_shows_cache_state(self):
        """String representation should show useful cache information."""
        cache = LRUCache(3)
        cache.put("a", "1")
        cache.put("b", "2")

        repr_str = repr(cache)
        assert "LRUCache" in repr_str
        assert "capacity=3" in repr_str
        assert "size=2" in repr_str
        assert "['a', 'b']" in repr_str
