---
draft: true
---

# LRU (Least Recently Used) — Implementation Notes

This folder contains small, self-contained LRU cache implementations in Python and Java. Each example includes a tiny test in the program’s main entry point so you can run it directly.

What LRU does
- Evicts the least recently used entry when capacity is full.
- Works well when “recent use ≈ likely to be used again soon.”
- Can be fooled by sequential scans (use 2Q/ARC/CLOCK variants for scan resistance).

Complexity goals
- get(key): O(1)
- put(key, value): O(1)

Two common implementations
1) HashMap + Doubly Linked List (from scratch)
   - HashMap maps keys → node references in a doubly-linked list.
   - On access, move node to the front (MRU); tail holds LRU for eviction.
2) LinkedHashMap (Java) or OrderedDict (Python)
   - Uses standard library data structures that maintain recency order.

How to run
- Python: `python lru.py`
- Java: `javac Lru.java && java Lru`

References back to the docs
- See “Eviction policies” in: ../readme.mdx

Further reading
- Size-aware LRU (weigh by object size to optimize byte hit ratio)
- Segmented LRU (2Q) for scan resistance