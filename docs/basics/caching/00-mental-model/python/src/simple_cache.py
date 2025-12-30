"""
Mental Model: Simple in-memory cache demonstrating basic cache operations.

This represents the "librarian's quick shelf" - a fast storage layer that
remembers frequently accessed data to avoid expensive backend operations.
"""


class SimpleCache:
    """
    A simple in-memory key-value cache.

    Demonstrates the fundamental cache operations:
    - put: Store data for fast retrieval
    - get: Retrieve data instantly (cache hit) or None (cache miss)
    """

    def __init__(self):
        """Initialize empty cache storage."""
        self._storage = {}

    def get(self, key):
        """
        Retrieve a value from the cache.

        Args:
            key: The key to look up

        Returns:
            The cached value if present, None otherwise (cache miss)
        """
        return self._storage.get(key)

    def put(self, key, value):
        """
        Store a value in the cache.

        Args:
            key: The key to store under
            value: The value to cache
        """
        self._storage[key] = value

    def size(self):
        """
        Return the number of items currently cached.

        Returns:
            int: Number of cached items
        """
        return len(self._storage)

    def contains_key(self, key):
        """
        Check if a key exists in the cache.

        Args:
            key: The key to check

        Returns:
            bool: True if key exists, False otherwise
        """
        return key in self._storage

    def clear(self):
        """Clear all cached data."""
        self._storage.clear()
