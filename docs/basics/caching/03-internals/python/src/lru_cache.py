"""
LRU Cache implementation demonstrating eviction policies and cache internals.

Uses OrderedDict to maintain access order efficiently.
Shows the trade-offs between memory usage, performance, and eviction accuracy.
"""

from collections import OrderedDict


class LRUCache:
    """
    LRU (Least Recently Used) Cache implementation.

    Evicts least recently accessed items when capacity is exceeded.
    Demonstrates the internals of eviction policies and their trade-offs.
    """

    def __init__(self, capacity):
        """
        Create LRU cache with specified capacity.

        Args:
            capacity: Maximum number of entries (must be positive)

        Raises:
            ValueError: If capacity is not positive
        """
        if capacity <= 0:
            raise ValueError("Capacity must be positive")

        self.capacity = capacity
        # OrderedDict maintains insertion order, we'll use it for access order
        self.cache = OrderedDict()

    def get(self, key):
        """
        Get value from cache, updating access order.

        Args:
            key: The key to look up

        Returns:
            Cached value or None if not present
        """
        if key in self.cache:
            # Move to end (most recently used)
            self.cache.move_to_end(key)
            return self.cache[key]
        return None

    def put(self, key, value):
        """
        Put value in cache, evicting LRU entry if necessary.

        Args:
            key: The key
            value: The value
        """
        if key in self.cache:
            # Update existing, move to end
            self.cache[key] = value
            self.cache.move_to_end(key)
        else:
            self.cache[key] = value

        # Evict if over capacity
        if len(self.cache) > self.capacity:
            # Remove least recently used (first item)
            self.cache.popitem(last=False)

    def contains_key(self, key):
        """Check if key exists in cache."""
        return key in self.cache

    @property
    def size(self):
        """Get current cache size."""
        return len(self.cache)

    def is_empty(self):
        """Check if cache is empty."""
        return len(self.cache) == 0

    def clear(self):
        """Clear all entries from cache."""
        self.cache.clear()

    @property
    def utilization(self):
        """Get current utilization as fraction (0.0 to 1.0)."""
        return len(self.cache) / self.capacity

    def keys_in_lru_order(self):
        """
        Get all keys in LRU order (eldest first).

        Returns:
            List of keys in LRU order (oldest to newest)
        """
        return list(self.cache.keys())

    def __repr__(self):
        """String representation showing cache state."""
        keys = list(self.cache.keys())
        return f"LRUCache(capacity={self.capacity}, size={len(self.cache)}, keys={keys})"
