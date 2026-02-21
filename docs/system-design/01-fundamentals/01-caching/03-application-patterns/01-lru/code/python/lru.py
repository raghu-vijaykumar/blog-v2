"""
LRU cache using Python's OrderedDict (O(1) get/put under typical assumptions).

Run: python lru.py
"""
from collections import OrderedDict


class LRU:
    def __init__(self, capacity: int):
        assert capacity > 0
        self.cap = capacity
        self.map: OrderedDict = OrderedDict()

    def get(self, key):
        if key not in self.map:
            return None
        # move to end to mark as recently used
        val = self.map.pop(key)
        self.map[key] = val
        return val

    def put(self, key, value):
        if key in self.map:
            self.map.pop(key)
        elif len(self.map) >= self.cap:
            # popitem(last=False) pops LRU
            self.map.popitem(last=False)
        self.map[key] = value


def _run_basic_tests():
    lru = LRU(2)
    lru.put("a", 1)
    lru.put("b", 2)
    assert lru.get("a") == 1  # a is MRU, b becomes LRU
    lru.put("c", 3)           # evicts b
    assert lru.get("b") is None
    assert lru.get("a") == 1
    assert lru.get("c") == 3
    lru.put("d", 4)           # evicts a
    assert lru.get("a") is None
    assert lru.get("c") == 3
    assert lru.get("d") == 4
    print("LRU basic tests passed.")


if __name__ == "__main__":
    _run_basic_tests()
