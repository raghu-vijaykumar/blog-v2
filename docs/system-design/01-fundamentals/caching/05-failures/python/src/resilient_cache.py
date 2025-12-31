from typing import Dict, TypeVar, Generic, Callable, Any
import threading

K = TypeVar('K')
V = TypeVar('V')


class ResilientCache(Generic[K, V]):
    def __init__(self, max_failures: int = 3):
        self._storage: Dict[K, V] = {}
        self._max_failures = max_failures
        self._failure_count = 0
        self._circuit_open = False
        self._lock = threading.Lock()

    def get(self, key: K) -> V:
        if self._circuit_open:
            raise RuntimeError("Circuit breaker open - cache unavailable")
        return self._storage.get(key)

    def put(self, key: K, value: V) -> None:
        if self._circuit_open:
            raise RuntimeError("Circuit breaker open - cache unavailable")
        self._storage[key] = value

    def get_with_circuit_breaker(self, key: K, fallback: Callable[[], V]) -> V:
        """Circuit breaker pattern with graceful degradation"""
        if self._circuit_open:
            try:
                return fallback()  # Graceful degradation
            except Exception as e:
                # Even in circuit open state, if fallback fails, we might want to track this
                # but for now, just re-raise
                raise e

        try:
            result = self._storage.get(key)
            if result is not None:
                with self._lock:
                    self._failure_count = 0  # Reset on success
                return result

            # Key not in cache, try fallback
            result = fallback()
            with self._lock:
                self._failure_count = 0  # Reset on success
            return result

        except Exception as e:
            with self._lock:
                self._failure_count += 1
                if self._failure_count >= self._max_failures:
                    self._circuit_open = True
            raise e

    def warm(self, data_loader: Callable[[], Dict[K, V]]) -> None:
        """Cache warming - pre-populate cache to avoid cold starts"""
        try:
            warm_data = data_loader()
            self._storage.update(warm_data)
        except Exception as e:
            # Log warning but don't fail - cache can still serve requests
            print(f"Cache warming failed: {e}")

    def reset(self) -> None:
        """Reset circuit breaker (for testing/admin purposes)"""
        with self._lock:
            self._circuit_open = False
            self._failure_count = 0

    @property
    def is_circuit_open(self) -> bool:
        return self._circuit_open

    @property
    def failure_count(self) -> int:
        return self._failure_count
