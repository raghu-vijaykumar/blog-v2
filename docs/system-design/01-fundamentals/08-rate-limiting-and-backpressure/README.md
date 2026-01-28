---
title: Rate Limiting & Backpressure
---

# Rate Limiting & Backpressure

What to cover
- Token/leaky bucket, fixed/sliding window, GCRA
- Per-tenant and per-endpoint limits; fairness; burst handling
- Backpressure signals: 429s, queue depth, shed load; circuit breaker tie-ins
- Distributed enforcement: centralized vs decentralized counters, sharding

Next steps: Implementation patterns with Envoy/Nginx and app-level libraries.
