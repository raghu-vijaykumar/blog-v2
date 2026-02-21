---
draft: true
---

# 2Q (Segmented LRU) — Implementation & Usage

What it is
- Two segments: A1 (probationary) for new entries, Am (protected) for entries that have been accessed twice.
- New items go to A1; a second touch promotes them to Am. This resists scan pollution better than pure LRU.

When to use
- Mixed workloads with sequential scans and locality; want better hit ratio than LRU with low overhead.

Complexity (target)
- get/put: O(1) using two LRU structures + a map.

Run the examples
- Python: `python code/python/twoq.py`
- Java: `javac code/java/TwoQ.java && java TwoQ`

References back to docs
- See “Eviction policies” in: ../readme.mdx
