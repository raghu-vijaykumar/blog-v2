---
title: Multi-level and Hierarchical Caching
---

# Multi-level and hierarchical caching

Layering
- Browser → CDN → reverse proxy → service cache → DB
- Responsibilities: privacy/auth at the edge, compute offload at origin, request/byte hit goals

Coordination
- Short TTLs at edge, longer TTLs at origin
- Bypass heuristics for low-hit or personalized traffic

Rules of thumb
- Immutable assets: year-long TTL + versioned filenames
- Personalized responses: no-store at edge, consider in-process cache within session
