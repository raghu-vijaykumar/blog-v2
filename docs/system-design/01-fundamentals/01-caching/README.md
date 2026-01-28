---
title: Caching Fundamentals
---

# Caching Fundamentals

This guide outlines what to learn about caching from a system design point of view: what to cache, where to place caches, how to keep them consistent, and how to operate them at scale.

## Learning outcomes
- Decide cache placement across client → CDN/edge → reverse proxy → app → DB
- Choose patterns: cache-aside, read-/write-through/back/around, refresh-ahead
- Design keys, TTLs, eviction/admission policies; avoid stampedes and hot keys
- Measure hit ratios and size caches; reason about consistency and invalidation

## Module 1: Foundations and mental models
- Why caching works: locality of reference, read/write ratios, data volatility classes
- Cost model: latency tiers, network vs compute vs storage trade-offs
- Cache by location: browser, CDN/edge, reverse proxy (Varnish/Envoy), in-process, distributed cache (Redis/Memcached), DB page/buffer cache
- What to cache: objects vs query results vs fragments; negative caching; idempotency considerations

## Module 2: HTTP, CDN and edge caching
- HTTP semantics: Cache-Control, Expires, ETag/If-None-Match, Last-Modified, Vary
- Freshness vs validation; conditional requests and revalidation flows
- CDN strategies: pull vs push, purging/invalidation, cache keys, signed URLs/cookies, privacy and PII
- Patterns: stale-while-revalidate, stale-if-error, immutable assets, versioned file names
- Pitfalls: cache poisoning, personalized content leakage, Vary explosion

## Module 3: Application-level patterns
- Cache-aside vs read-through; write-through vs write-back vs write-around
- TTL design: hard vs soft TTL, jitter to prevent thundering herds, adaptive TTLs
- Eviction/admission: LRU/LFU/ARC, TinyLFU/Windowed TinyLFU, size-aware eviction, doorkeeper/Bloom filter
- Stampede prevention: request coalescing/singleflight, per-key locks, early refresh
- Data modeling: key schema (namespaces, versioning, multitenancy), object vs field-level caching

## Module 4: Distributed caches in production (Redis/Memcached)
- Topologies: standalone, Sentinel HA, Redis Cluster sharding, consistent hashing, hash tags
- Replication/failover; persistence (RDB, AOF), fsync policies, durability vs latency
- Memory management: eviction policies (noeviction, allkeys-*, volatile-*), fragmentation, sizing
- Hot key mitigation: key hashing, replicated reads, local + remote hybrid caches, rate limiting
- Operations: connection pools, pipelining, timeouts/backpressure, slowlog, monitoring

## Module 5: Consistency, coherence, invalidation
- Read-your-write, monotonic reads, eventual vs strong considerations for caches
- Invalidation: write-through invalidation, pub/sub fanout, versioned keys, namespace busting
- Rolling deploys & schema changes: key version bumps, shadow caches
- Transactional updates: outbox-driven invalidation, idempotent writers

## Module 6: Multi-level and hierarchical caching
- Browser → CDN → reverse proxy → service cache → DB: responsibilities and TTL layering
- Coordinating layers: short TTLs at edge, longer at origin; request vs byte hit ratio goals
- When to bypass a layer; privacy/auth boundaries; cache-bypass heuristics

## Module 7: Observability and capacity planning
- Metrics: request and byte hit ratio, revalidation rate, origin offload, p95/p99 latencies
- Miss taxonomy: cold start, eviction/compaction, keyspace churn, configuration errors
- Sizing: cardinality, object size distribution (p50/p95), TTL → working set → RAM model
- Dashboards/alerts: hot key detection, stampede indicators, saturation and error budget impact

## Module 8: Security, privacy, multitenancy
- Auth + caching: token binding, safe key scoping, avoiding Authorization in Vary
- Cache poisoning and XS-Leaks risks; safe patterns for personalized content
- Tenant isolation: per-tenant namespaces, quotas, noisy neighbor controls

## Module 9: Databases and search (adjacent topics)
- Query result caching vs materialized views; read replicas vs caches
- DB page/buffer cache vs app cache; invalidation triggers; write amplification trade-offs
- Search result caching (query normalization), pagination freshness pitfalls

## Module 10: Case studies and “when not to cache”
- News feed, product detail pages, pricing/quoting, feature flags
- Freshness vs cost curves; bursty vs steady traffic; small working set edge cases

## Hands-on practice (pick 3–4)
- Configure HTTP/CDN caching with Cache-Control/ETag, SWR, and signed URLs; measure byte hit ratio
- Implement cache-aside with Redis including per-key singleflight and soft TTL refresh-ahead
- Design a key schema with namespaces/versioning and multi-tenant isolation; do a rolling deploy with a version bump
- Build hot-key detection and mitigation (replicated reads or request shedding)
- Instrument hit ratios and origin load; size the cache using traffic × TTL × size distribution

## Interview framing checklist
- Placement and access pattern → propose pattern (cache-aside/read-through)
- Keys/TTL/eviction/admission → stampede + hot key mitigation
- Consistency + invalidation plan → metrics/capacity → security/privacy risks

## References
- RFC 9111 (HTTP Caching); Google Web Fundamentals on caching
- Redis docs: eviction, persistence, cluster, latency tuning; TinyLFU/WTinyLFU papers
- Designing Data-Intensive Applications (Ch. 5, 11)

---

Next steps
- Use the left sidebar to navigate modules 1–10.
- When you’re ready to add runnable code, we’ll create docs/system-design/01-fundamentals/caching/code/ and place Java/Python examples there alongside these notes.
