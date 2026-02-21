---
draft: true
---

# LFU (Least Frequently Used) — Implementation Notes

Goal
- Evict the least frequently used key on capacity pressure; break ties by recency.

When to use
- When long-lived popularity matters more than short-term bursts.
- Beware of holding onto once-hot-now-cold items (consider aging/decay).

Complexity targets
- get/put: O(1) using key → (value, frequency) map, plus a map of frequency → ordered keys, and a minFreq pointer.

How to run
- Python: `python lfu.py`
- Java: `javac Lfu.java && java Lfu`

References back to the docs
- See “Eviction policies” in: ../readme.mdx

Example (capacity = 2)
- put(1,A) → cache = `{1:A(freq=1)}`
- put(2,B) → cache = `{1:A(f1), 2:B(f1)}`
- get(1)   → A (freq of 1 becomes 2): `{1:A(f2), 2:B(f1)}`
- put(3,C) → evict LFU (key 2 with f1); cache = `{1:A(f2), 3:C(f1)}`
- get(3)   → C (freq of 3 becomes 2)
- put(4,D) → LFU tie between 1 and 3 (both f2) → evict LRU among them (implementation detail); insert 4
