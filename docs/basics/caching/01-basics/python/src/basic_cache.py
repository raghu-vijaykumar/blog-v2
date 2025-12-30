"""
Basic Cache with Hit/Miss Tracking.

Demonstrates fundamental cache operations and statistics collection.
This builds on the mental model by adding observability.
"""


class BasicCache:
    """
    A basic cache with hit/miss tracking and statistics.

    Extends the mental model cache with monitoring capabilities
    essential for production cache management.
    """

    def __init__(self):
        """Initialize empty cache with statistics tracking."""
        self._storage = {}
        self._hits = 0
        self._misses = 0

    def get(self, key):
        """
        Retrieve a value from the cache, tracking hits and misses.

        Args:
            key: The key to look up

        Returns:
            The cached value if present, None otherwise (cache miss)
        """
        if key in self._storage:
            self._hits += 1
            return self._storage[key]
        self._misses += 1
        return None

    def put(self, key, value):
        """
        Store a value in the cache.

        Args:
            key: The key to store under
            value: The value to cache
        """
        self._storage[key] = value

    @property
    def hits(self):
        """Return the number of cache hits since creation."""
        return self._hits

    @property
    def misses(self):
        """Return the number of cache misses since creation."""
        return self._misses

    @property
    def hit_rate(self):
        """
        Calculate the cache hit rate.

        Returns:
            float: Hit rate as a fraction (0.0 to 1.0), or 0.0 if no requests
        """
        total = self._hits + self._misses
        return 0.0 if total == 0 else self._hits / total

    @property
    def size(self):
        """Return the current number of items in the cache."""
        return len(self._storage)

    def clear(self):
        """Clear all cached data and reset statistics."""
        self._storage.clear()
        self._hits = 0
        self._misses = 0


class CacheAsidePattern:
    """
    Demonstrates the cache-aside pattern - the most common caching integration.

    In this pattern, the application code explicitly manages cache and database.
    """

    def __init__(self, cache, database):
        """
        Initialize with cache and database (simulated).

        Args:
            cache: Cache instance to use
            database: Database instance (simulated as dict for demo)
        """
        self.cache = cache
        self.database = database  # Simulated database

    def get_user(self, user_id):
        """
        Get user using cache-aside pattern.

        1. Check cache first
        2. If miss, fetch from database
        3. Store result in cache
        4. Return data

        Args:
            user_id: The user ID to retrieve

        Returns:
            User data dict or None if not found
        """
        # Try cache first
        user = self.cache.get(user_id)
        if user is not None:
            return user

        # Cache miss - fetch from database
        user = self.database.get(user_id)
        if user is not None:
            # Store in cache for future requests
            self.cache.put(user_id, user)

        return user
