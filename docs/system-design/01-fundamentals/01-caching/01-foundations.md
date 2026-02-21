---
title: Foundations and Mental Models
---

# Foundations and mental models

This module explains the “why” and “how to think” about caching in plain language, before we get into specific tech.

## What caching really does
- Trade a small amount of staleness and memory for large wins in latency, throughput, and cost.
- Exploit the fact that many reads repeat (locality), while writes are rarer.
- Move work to cheaper/faster layers (RAM, edge) and reduce load on expensive origins (databases, remote services).

### Locality: the engine behind caches
- Temporal locality: if you needed an item recently, you’ll likely need it again soon (e.g., product page viewed many times in an hour).
- Spatial locality: if you needed item A, you’re likely to need nearby items B/C (e.g., page 1 then page 2, or related products).
- Working set: the distinct items actively used within a time window. Caches are effective when the working set fits in memory.

### The three levers that determine cache success
- Reuse: expected hit ratio. More repetition → better cache.
- Freshness tolerance: how stale can data be without harming correctness or UX (your “staleness budget”).
- Cost differential: how much slower/more expensive is origin vs cache (ms saved, CPU saved, cost saved per hit).

### Placement: where can you put caches?
- Browser: great for static assets and user-specific session data; zero network round-trip.
- CDN/Edge: best for public static content or cacheable APIs; offloads origin globally.
- Reverse proxy (e.g., Varnish/Envoy): origin-facing cache for HTTP responses.
- In-process (per-instance memory): micro-optimizations, very fast, but not shared across instances.
- Distributed cache (Redis/Memcached): shared across instances, controllable TTL/eviction, great for objects/queries.
- Database page/buffer cache: automatic at the DB layer; complements, not replaces, app-level caches.

### What to cache (and what not to)
- Best candidates: frequently-read, not-too-large, not-too-volatile items (e.g., product detail, user profile, config, feature flags with short TTL).
- Negative caching: cache “not found” briefly to prevent repeated misses thundering the origin.
- Beware: highly personalized responses at public layers (CDN) can leak data; cache keys must include the right context.
- Avoid: extremely volatile values that must be strictly fresh (e.g., stock balances or real-time prices) unless TTL is tiny and correctness rules allow staleness.

### Picking a TTL (freshness window)
- Classify volatility:
  - Immutable: never changes (versioned assets). TTL can be months; mark immutable.
  - Slowly changing: changes minutes–hours (profiles, catalog). TTL minutes–hours, consider stale-while-revalidate.
  - Highly volatile: seconds-level changes (prices, inventory). TTL seconds or use validation (ETag) instead of pure freshness.
- Use soft TTL with refresh-ahead for critical paths: serve slightly stale data for a short window while refreshing in background.
- Add jitter (±5–10%) to TTLs so many keys don’t expire at the same moment (prevents stampedes).

### Key design: how you name cached data
- Include a stable namespace and version: `entity:v1:<id>`. Bump version on schema/format changes.
- Include multitenancy context if needed: `tenant:<t>:entity:v1:<id>`.
- Keep keys short but descriptive. Avoid user secrets in keys.

### Basic math you’ll use a lot
- Hit ratio (HR) = cache_hits / total_requests. Aim high but not at the expense of correctness.
- Working set sizing (very rough):
  - Unique keys within TTL ≈ requests_per_sec × miss_rate × TTL_seconds (adjust for skew/Zipf distributions).
  - RAM needed ≈ unique_keys × average_item_size_bytes × overhead_factor (1.2–2.0).
- Example: 500 RPS, 20% miss rate, TTL 300s → unique ≈ 500 × 0.2 × 300 = 30,000 keys. If avg item 2 KB and overhead 1.5 → RAM ≈ 30,000 × 2 KB × 1.5 ≈ 90 MB.

### When caching fails (anti-patterns)
- Low reuse: nearly every request is unique → HR stays low, memory is wasted.
- Over-personalization at public layers: privacy risk and low reuse.
- No invalidation owner: data changes without cache coordination → stale or incorrect results.
- Oversized objects: few big values evict the many small hot ones; split or compress.

### A simple mental model to choose your first cache
- If content is public and mostly static → start at CDN with long TTL + versioned assets.
- If content is dynamic but read-heavy → add a distributed cache (Redis) with cache-aside and soft TTL.
- If personalization prevents edge caching → keep edge private/no-store and cache safely inside the service boundary with scoped keys.

## Quick start checklist
- Define staleness budget (how stale is acceptable?)
- Decide placement (edge, proxy, in-app, Redis)
- Pick TTL strategy (hard vs soft + SWR) and add jitter
- Design keys with versioning and tenancy scope
- Plan invalidation (owner, mechanism) and metrics (hits, misses, evictions, hot keys)