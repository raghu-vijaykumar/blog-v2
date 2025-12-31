import pytest
from src.resilient_cache import ResilientCache


class TestResilientCache:

    def setup_method(self):
        self.cache = ResilientCache(max_failures=3)

    def test_normal_operation(self):
        self.cache.put("key1", "value1")
        assert self.cache.get("key1") == "value1"
        assert not self.cache.is_circuit_open

    def test_circuit_breaker_opens_after_failures(self):
        # Simulate failures
        with pytest.raises(RuntimeError):
            self.cache.get_with_circuit_breaker("missing", lambda: (_ for _ in ()).throw(RuntimeError("Backend failure")))

        with pytest.raises(RuntimeError):
            self.cache.get_with_circuit_breaker("missing", lambda: (_ for _ in ()).throw(RuntimeError("Backend failure")))

        # Third failure should open circuit
        with pytest.raises(RuntimeError):
            self.cache.get_with_circuit_breaker("missing", lambda: (_ for _ in ()).throw(RuntimeError("Backend failure")))

        assert self.cache.is_circuit_open

    def test_graceful_degradation_when_circuit_open(self):
        # Open the circuit
        for _ in range(3):
            try:
                self.cache.get_with_circuit_breaker("missing", lambda: (_ for _ in ()).throw(RuntimeError("Backend failure")))
            except RuntimeError:
                pass  # Expected

        # Now circuit should be open, fallback should be used
        result = self.cache.get_with_circuit_breaker("missing", lambda: "fallback-value")
        assert result == "fallback-value"

    def test_cache_warming(self):
        warm_data = {"warm-key": "warm-value"}

        self.cache.warm(lambda: warm_data)

        assert self.cache.get("warm-key") == "warm-value"

    def test_cache_warming_failure(self):
        # Warming fails but cache should still work
        self.cache.warm(lambda: (_ for _ in ()).throw(RuntimeError("Data loader failed")))

        # Cache should still accept new puts
        self.cache.put("key", "value")
        assert self.cache.get("key") == "value"

    def test_circuit_breaker_reset(self):
        # Open circuit
        for _ in range(3):
            try:
                self.cache.get_with_circuit_breaker("missing", lambda: (_ for _ in ()).throw(RuntimeError("Backend failure")))
            except RuntimeError:
                pass  # Expected

        assert self.cache.is_circuit_open

        # Reset
        self.cache.reset()
        assert not self.cache.is_circuit_open

        # Should work again
        self.cache.put("key", "value")
        assert self.cache.get("key") == "value"

    def test_failure_count_tracking(self):
        assert self.cache.failure_count == 0

        try:
            self.cache.get_with_circuit_breaker("missing", lambda: (_ for _ in ()).throw(RuntimeError("Backend failure")))
        except RuntimeError:
            pass  # Expected

        assert self.cache.failure_count == 1
